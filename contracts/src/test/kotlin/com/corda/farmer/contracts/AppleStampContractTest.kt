package com.corda.farmer.contracts

import com.corda.farmer.states.AppleStamp
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.CordaX500Name
import net.corda.testing.core.TestIdentity
import net.corda.testing.node.MockServices
import net.corda.testing.node.ledger
import org.junit.Test

internal class AppleStampContractTest {
    private val ledgerServices = MockServices()
    private val farmerBob = TestIdentity(CordaX500Name("Bob Farm's","London","GB"))
    private val appleJuice = TestIdentity(CordaX500Name("Apple Juice","London","GB"))

    @Test
    fun `should verify transaction when at least one command is part of the transaction`(){
        val stamp =
            AppleStamp("Yellow Apple - 200", farmerBob.party, appleJuice.party, UniqueIdentifier())
        ledgerServices.ledger {
            transaction {
                output(AppleStampContract.ID, stamp)
                command(listOf(appleJuice.publicKey, farmerBob.publicKey),AppleStampContract.Commands.Issue())
                verifies()
            }
        }
    }

    @Test
    fun `should fail transaction when AppleStamp doesn't have description`(){
        val stamp =
            AppleStamp("",appleJuice.party, farmerBob.party, UniqueIdentifier())
        ledgerServices.ledger {
            transaction {
                output(AppleStampContract.ID, stamp)
                command(listOf(appleJuice.publicKey, farmerBob.publicKey),AppleStampContract.Commands.Issue())
                fails()
            }
        }
    }

    @Test
    fun `should fail transaction when more than one AppleStamps are applied to the transaction`(){
        val stamp =
            AppleStamp("Yellow Apples - 12",appleJuice.party, farmerBob.party, UniqueIdentifier())
        val stamp2 =
            AppleStamp("Red Apples - 10",appleJuice.party, farmerBob.party, UniqueIdentifier())

        ledgerServices.ledger {
            transaction {
                output(AppleStampContract.ID, stamp)
                output(AppleStampContract.ID, stamp2)
                command(listOf(appleJuice.publicKey, farmerBob.publicKey),AppleStampContract.Commands.Issue())
                fails()
            }
        }
    }
}