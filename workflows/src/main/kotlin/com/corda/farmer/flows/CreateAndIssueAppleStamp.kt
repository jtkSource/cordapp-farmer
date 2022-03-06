package com.corda.farmer.flows

import co.paralleluniverse.fibers.Suspendable
import com.corda.farmer.contracts.AppleStampContract
import com.corda.farmer.states.AppleStamp
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder


class CreateAndIssueAppleStamp {

    @InitiatingFlow //This indicates that this flow is the initiating flow
    @StartableByRPC //This annotation allows the flow to be started by RPC
    class CreateAndIssueAppleStampInitiator(val stampDescription: String, val holder: Party)
        : FlowLogic<SignedTransaction>(){

        @Suspendable
        @Throws(FlowException::class)
        override fun call(): SignedTransaction {
            /* Obtain a reference to a notary we wish to use.
             * METHOD 1: Take first notary on network, WARNING: use for test, non-prod environments, and single-notary networks only!*
             *  METHOD 2: Explicit selection of notary by CordaX500Name - argument can by coded in flows or parsed from config (Preferred)
             *   - For production you always want to use Method 2 as it guarantees the expected notary is returned.
             */
            val notary: Party = serviceHub.networkMapCache.notaryIdentities[0]
            val uniqueID: UniqueIdentifier = UniqueIdentifier()
            val newStamp: AppleStamp = AppleStamp(stampDescription, ourIdentity, holder, uniqueID)
            //In transactions with multiple parties, you need a notary service to reach consensus between the parties
            val txBuilder = TransactionBuilder(notary)
                //Use the addOutputState method to add the newStamp.
                .addOutputState(newStamp)
                // Use the addCommand method to add the Issue command and a list of required signees (the initiator and the holder).
                .addCommand(AppleStampContract.Commands.Issue(),
                //  Use getOurIdentity and getOwningKey methods to get the required signees.
                     listOf(ourIdentity.owningKey, holder.owningKey))
            // Use the verify method to trigger contract verification of the txBuilder from the getServiceHub.
            txBuilder.verify(serviceHub)

            //The initiator needs to sign the transaction for the transaction to be valid.

            val partSignedTx : SignedTransaction = serviceHub.signInitialTransaction(txBuilder)
            // Send the state to the counterparty, and receive it back with their signature.
            // Start a FlowSession with the counterparty using the InitiateFlow method
            val otherPartySession: FlowSession = initiateFlow(holder)
            // Call a subflow to collect signatures
            val fullySignedTx: SignedTransaction = subFlow(CollectSignaturesFlow(partSignedTx,listOf(otherPartySession)))
            // Notarise and record the transaction in both parties' vaults.
            return subFlow(FinalityFlow(fullySignedTx, listOf(otherPartySession)))
        }
    }

    //initiator flow needs a corresponding responder flow. The counterparty runs the responder flow.
    @InitiatedBy(CreateAndIssueAppleStampInitiator::class)
    class CreateAndIssueAppleStampResponder(val counterpartySession: FlowSession) : FlowLogic<Unit>(){

        @Suspendable
        override fun call() {

            val signedTransaction: SignedTransaction = subFlow(object : SignTransactionFlow(counterpartySession){
                override fun checkTransaction(stx: SignedTransaction) {
                    /*
                     * SignTransactionFlow will automatically verify the transaction and its signatures before signing it.
                     * However, just because a transaction is contractually valid doesn’t mean we necessarily want to sign.
                     * What if we don’t want to deal with the counterparty in question, or the value is too high,
                     * or we’re not happy with the transaction’s structure? checkTransaction
                     * allows us to define these additional checks. If any of these conditions are not met,
                     * we will not sign the transaction - even if the transaction and its signatures are contractually valid.
                     * ----------
                     * For this hello-world cordapp, we will not implement any additional checks.
                    */
                }
            })
            //Stored the transaction into database.
            subFlow(ReceiveFinalityFlow(counterpartySession, signedTransaction.id))
        }
    }

}