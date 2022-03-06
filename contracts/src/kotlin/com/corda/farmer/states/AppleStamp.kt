package com.corda.farmer.states

import com.corda.farmer.contracts.AppleStampContract
import net.corda.core.contracts.BelongsToContract
import net.corda.core.contracts.LinearState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.Party

@BelongsToContract(AppleStampContract::class)
data class AppleStamp(
    val stampDesc: String,//For example: "One stamp can be exchanged for a basket of Gala apples."
    val issuer: Party,//The person who issued the stamp.
    var holder: Party,//The person who currently owns the stamp.
    override val linearId: UniqueIdentifier //All LinearStates must have a variable for the state’s linear ID.
) : LinearState {
    //All Corda states must include a parameter to indicate the parties that store the states
    override val participants: List<AbstractParty> = listOf(issuer, holder)
}
