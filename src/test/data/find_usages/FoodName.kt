class Jungle {
    var animals: List<Animal>? = null
}

class Animal(var animalName: String = "", var foods: List<Food>? = null) {
    var animalNames: String = ""
}

data class Food(val na<caret>me: String, val type: Type)

data class Type(val name: String)