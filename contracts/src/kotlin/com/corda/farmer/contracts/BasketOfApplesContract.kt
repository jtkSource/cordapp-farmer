package com.corda.farmer.contracts

import com.corda.farmer.states.AppleStamp
import com.corda.farmer.states.BasketOfApples
import net.corda.core.contracts.CommandData
import net.corda.core.contracts.Contract
import net.corda.core.contracts.requireThat
import net.corda.core.transactions.LedgerTransaction

class BasketOfApplesContract: Contract {
    companion object {
        @JvmStatic
        val ID: String = "com.corda.farmer.contracts.BasketOfApplesContract"
    }

    override fun verify(tx: LedgerTransaction) {
        when(tx.commands[0].value){
            is Commands.PackBasket -> {
                val output: BasketOfApples = tx.outputsOfType(BasketOfApples::class.java)[0]
                requireThat {
                    "This transaction should only output one BasketOfApples state" using (tx.outputs.size == 1)
                    "The output BasketOfApples state should have clear description of Apple product" using (output.description != "")
                    "The output BasketOfApples state should have non zero weight" using (output.weight > 0)
                    "The Owner of BasketOfApples should be empty" using (output.owner == null)
                }
            }
            is Commands.Redeem -> {
                val input : AppleStamp = tx.inputsOfType(AppleStamp::class.java)[0]
                val output : BasketOfApples = tx.outputsOfType(BasketOfApples::class.java)[0]
                requireThat {
                    "This transaction should consume two states" using (tx.inputStates.size ==2)
                    "The issuer of the Apple stamp should be the producing farm of this basket of apple" using (input.issuer == output.farm)
                    "The holder of the Apple stamp shouldn't be the producing farm of this basket of apple" using (input.holder != output.farm)
                    "The basket of apple has to weight more than 0" using (output.weight > 0)
                }
            }
            else -> throw IllegalArgumentException("Incorrect type of BasketOfApples Commands")
        }
    }

    interface Commands: CommandData{
        class PackBasket : Commands
        class Redeem : Commands
    }
}