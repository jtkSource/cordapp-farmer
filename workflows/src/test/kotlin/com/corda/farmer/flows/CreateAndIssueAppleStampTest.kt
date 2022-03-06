package com.corda.farmer.flows

import com.corda.farmer.flows.CreateAndIssueAppleStamp.CreateAndIssueAppleStampInitiator
import com.corda.farmer.states.AppleStamp
import net.corda.core.node.services.Vault.StateStatus
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.testing.node.MockNetwork
import net.corda.testing.node.MockNetworkParameters
import net.corda.testing.node.StartedMockNode
import net.corda.testing.node.TestCordapp
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.util.concurrent.ExecutionException


internal class CreateAndIssueAppleStampTest {
    private lateinit var network: MockNetwork
    private lateinit var a: StartedMockNode
    private lateinit var b: StartedMockNode

    @Before
    fun setup() {
        network = MockNetwork(
            MockNetworkParameters(
                cordappsForAllNodes = listOf(TestCordapp.findCordapp("com.corda.farmer.contracts"),
                    TestCordapp.findCordapp("com.corda.farmer.flows"))))
        a = network.createPartyNode(null)
        b = network.createPartyNode(null)
        network.runNetwork()
    }

    @After
    fun tearDown() {
        network.stopNodes()
    }

    @Test
    @Throws(ExecutionException::class, InterruptedException::class)
    fun `CreateAndIssueAppleStampTest`() {
        val flow1 = CreateAndIssueAppleStampInitiator(
            "HoneyCrispy 4072", this.b.info.legalIdentities.get(0)
        )
        val future1 = a.startFlow(flow1)
        network.runNetwork()

        //successful query means the state is stored at node b's vault. Flow went through.

        //successful query means the state is stored at node b's vault. Flow went through.
        val inputCriteria: QueryCriteria = QueryCriteria.VaultQueryCriteria()
            .withStatus(StateStatus.UNCONSUMED)
        val (stampDesc) = b.services.vaultService
            .queryBy(AppleStamp::class.java, inputCriteria).states[0].state.data
        assert(stampDesc == "HoneyCrispy 4072")
    }
}