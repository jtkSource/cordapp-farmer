package com.corda.farmer.contracts

import com.corda.farmer.states.AppleStamp
import com.corda.farmer.states.BasketOfApples
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.CordaX500Name
import net.corda.testing.core.TestIdentity
import net.corda.testing.node.MockServices
import net.corda.testing.node.ledger
import org.junit.Before
import org.junit.Test
import org.junit.jupiter.api.Assertions.*

internal class BasketOfApplesContractTest {
    private val ledgerServices = MockServices()
    private val farmerBob = TestIdentity(CordaX500Name("Bob Farm's", "London", "GB"))
    private val appleJuice = TestIdentity(CordaX500Name("Apple Juice", "London", "GB"))

    @Test
    fun `transaction should only output one BasketOfApples state when command is PackBasket`() {
        val basketOfApples = BasketOfApples( "Basket of Yellow Baskets", farmerBob.party, 1 )
        ledgerServices.ledger {
            transaction {
                output(BasketOfApplesContract.ID, basketOfApples)
                command(listOf(farmerBob.publicKey), BasketOfApplesContract.Commands.PackBasket())
                verifies()
            }
        }
    }

    @Test
    fun `transaction should fail when description is empty in the BasketOfApples state when command is PackBasket`() {
        val basketOfApples = BasketOfApples( "", farmerBob.party, 1)
        ledgerServices.ledger {
            transaction {
                output(BasketOfApplesContract.ID, basketOfApples)
                command(listOf(farmerBob.publicKey), BasketOfApplesContract.Commands.PackBasket())
                fails()
            }
        }
    }

    @Test
    fun `transaction should pass when AppleStamp issuer is the BasketOfApples farm for  Redeem command`() {
        val basketOfApples = BasketOfApples("Basket of Yellow Apples", farmerBob.party, appleJuice.party, 200)
        val yellowAppleStamps = AppleStamp("Yellow Apple - 200", farmerBob.party, appleJuice.party, UniqueIdentifier())
        ledgerServices.ledger {
            transaction {
                output(BasketOfApplesContract.ID, basketOfApples)
                input(AppleStampContract.ID, yellowAppleStamps)
                command(listOf(farmerBob.publicKey), BasketOfApplesContract.Commands.Redeem())
                fails()
            }
        }
    }

    @Test
    fun `transaction should fail when AppleStamp issuer the same as the holder of the BasketOfApples for  Redeem command`() {
        val basketOfApples = BasketOfApples("Basket of Yellow Apples", farmerBob.party,appleJuice.party, 200)
        val yellowAppleStamps = AppleStamp("Yellow Apple - 200", farmerBob.party, farmerBob.party, UniqueIdentifier())
        ledgerServices.ledger {
            transaction {
                output(BasketOfApplesContract.ID, basketOfApples)
                input(AppleStampContract.ID, yellowAppleStamps)
                command(listOf(farmerBob.publicKey), BasketOfApplesContract.Commands.Redeem())
                fails()
            }
        }
    }

}