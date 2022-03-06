package com.corda.farmer.states

import com.corda.farmer.contracts.BasketOfApplesContract
import net.corda.core.contracts.BelongsToContract
import net.corda.core.contracts.ContractState
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.Party
import net.corda.core.serialization.ConstructorForDeserialization

/**
 * The BasketOfApples state is the basket of apples that Farmer Bob
 * self-issues to prepare the apples for Peter
 */
@BelongsToContract(BasketOfApplesContract::class)
data class BasketOfApples(
    val description: String,
    val farm: Party,
    val weight: Int): ContractState {
    var owner: Party? = null

    @ConstructorForDeserialization
    constructor(description: String, farm: Party, holder: Party?, weight: Int) : this(description, farm, weight){
        owner = holder
    }

    override val participants: List<AbstractParty> = listOf(farm)

    fun changeOwner(buyer: Party) = BasketOfApples(this.description, this.farm, buyer, this.weight)
}
