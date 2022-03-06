package com.corda.farmer.contracts

import com.corda.farmer.states.AppleStamp
import net.corda.core.contracts.CommandData
import net.corda.core.contracts.Contract
import net.corda.core.contracts.requireThat
import net.corda.core.transactions.LedgerTransaction


class AppleStampContract : Contract {
    companion object {
        @JvmStatic
        val ID: String = "com.corda.farmer.contracts.AppleStampContract"
    }

    override fun verify(tx: LedgerTransaction) {
        when(tx.commands[0].value){
            is Commands.Issue -> {
                val output: AppleStamp = tx.outputsOfType(AppleStamp::class.java)[0]
                requireThat {
                    "This transaction should only have one AppleStamp state as output" using (tx.outputs.size == 1)
                    "The output AppleStamp state should have clear description of the type of redeemable goods" using (output.stampDesc != "")
                }
            }
            is BasketOfApplesContract.Commands.Redeem ->{
                //Transaction verification will happen in BasketOfApple Contract
            }
            else -> throw IllegalArgumentException("Incorrect type of AppleStamp Commands")
        }
    }

    interface Commands : CommandData {
        class Issue : Commands
    }
}