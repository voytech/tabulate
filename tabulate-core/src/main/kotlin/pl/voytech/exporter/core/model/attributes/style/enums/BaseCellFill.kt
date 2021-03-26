package pl.voytech.exporter.core.model.attributes.style.enums

import pl.voytech.exporter.core.model.attributes.style.enums.contract.CellFill

enum class BaseCellFill(private val id:String) : CellFill{
    WIDE_DOTS("wide_dots"),
    LARGE_SPOTS("large_spots"),
    BRICKS("bricks"),
    DIAMONDS("diamonds"),
    SMALL_DOTS("small_dots"),
    SOLID("solid"),
    SQUARES("squares");

    override fun getAttributeId() = id
}