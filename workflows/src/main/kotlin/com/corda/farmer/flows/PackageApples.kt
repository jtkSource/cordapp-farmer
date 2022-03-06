package com.corda.farmer.flows

import co.paralleluniverse.fibers.Suspendable
import com.corda.farmer.contracts.BasketOfApplesContract
import com.corda.farmer.states.BasketOfApples
import net.corda.core.flows.FinalityFlow
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.InitiatingFlow
import net.corda.core.flows.StartableByRPC
import net.corda.core.identity.CordaX500Name
import net.corda.core.identity.Party
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder

class PackageApples {

    @InitiatingFlow
    @StartableByRPC
    class PackageApplesInitiator(val appleDescription: String, val weight: Int) : FlowLogic<SignedTransaction>() {

        @Suspendable
        override fun call(): SignedTransaction {
            //val notary: Party = serviceHub.networkMapCache.notaryIdentities[0]
            val notary: Party? = serviceHub.networkMapCache.getNotary(CordaX500Name.parse("O=Notary,L=London,C=GB")); // METHOD 2
            //Create the output object
            val basket = BasketOfApples(appleDescription, ourIdentity, weight)
            //Building transaction
            val txBuilder: TransactionBuilder = TransactionBuilder(notary)
                .addOutputState(basket)
                .addCommand(BasketOfApplesContract.Commands.PackBasket(), ourIdentity.owningKey)
            txBuilder.verify(serviceHub)
            val signedTransaction: SignedTransaction = serviceHub.signInitialTransaction(txBuilder)
            return subFlow(FinalityFlow(signedTransaction, listOf()))
        }

    }
}