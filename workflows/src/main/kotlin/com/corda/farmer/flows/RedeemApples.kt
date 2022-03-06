package com.corda.farmer.flows

import co.paralleluniverse.fibers.Suspendable
import com.corda.farmer.contracts.BasketOfApplesContract
import com.corda.farmer.states.AppleStamp
import com.corda.farmer.states.BasketOfApples
import net.corda.core.contracts.StateAndRef
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.flows.*
import net.corda.core.identity.CordaX500Name
import net.corda.core.identity.Party
import net.corda.core.node.services.Vault
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import java.util.*

class RedeemApples {
    @InitiatingFlow
    @StartableByRPC
    class RedeemApplesInitiator(val buyer: Party, val stampId : String) : FlowLogic<SignedTransaction>() {

        @Suspendable
        override fun call(): SignedTransaction {
            val notary: Party? = serviceHub.networkMapCache.getNotary(CordaX500Name.parse("O=Notary,L=London,C=GB")); // METHOD 2

            //Query AppleStamp
            val inputCriteria : QueryCriteria.LinearStateQueryCriteria = QueryCriteria.LinearStateQueryCriteria()
                .withUuid(listOf(UUID.fromString(stampId)))
                .withStatus(Vault.StateStatus.UNCONSUMED)
                .withRelevancyStatus(Vault.RelevancyStatus.RELEVANT)
            val appleStampStateAndRef : StateAndRef<*> = serviceHub.vaultService
                .queryBy(AppleStamp::class.java, inputCriteria).states[0]

            //Query BasketOfApples
            val outputCriteria: QueryCriteria.VaultQueryCriteria =
                QueryCriteria.VaultQueryCriteria()
                    .withStatus(Vault.StateStatus.UNCONSUMED)
                    .withRelevancyStatus(Vault.RelevancyStatus.RELEVANT)
            val basketOfAppleStateAndRef = serviceHub.vaultService
                .queryBy(BasketOfApples::class.java, outputCriteria).states[0]
            val originalBasketOfApples : BasketOfApples =  basketOfAppleStateAndRef.state.data

            //Modify output to address the owner change
            val output: BasketOfApples = originalBasketOfApples.changeOwner(buyer)

            val txBuilder: TransactionBuilder = TransactionBuilder(notary)
                .addInputState(appleStampStateAndRef)
                .addInputState(basketOfAppleStateAndRef)
                .addOutputState(output, BasketOfApplesContract.ID)
                .addCommand(BasketOfApplesContract.Commands.Redeem(), listOf(ourIdentity.owningKey, buyer.owningKey))
            txBuilder.verify(serviceHub)

            // Sign the transaction
            val partSignedTx: SignedTransaction = serviceHub.signInitialTransaction(txBuilder)
            val otherPartySession: FlowSession = initiateFlow(buyer)
            val fullySignedTx: SignedTransaction = subFlow(CollectSignaturesFlow(partSignedTx, listOf(otherPartySession)))
            return subFlow(FinalityFlow(fullySignedTx, listOf(otherPartySession)))
        }
    }

    @InitiatedBy(RedeemApplesInitiator::class)
    class RedeemApplesResponder(val counterPartySession: FlowSession) : FlowLogic<Unit>() {
        @Suspendable
        override fun call() {
            val signedTransaction: SignedTransaction = subFlow(object: SignTransactionFlow(counterPartySession){
                override fun checkTransaction(stx: SignedTransaction) { }
            })
            subFlow(ReceiveFinalityFlow(counterPartySession, signedTransaction.id))
        }
    }
}