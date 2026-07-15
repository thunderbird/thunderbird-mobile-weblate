package net.thunderbird.cli.importer.git

@JvmInline
value class Branch(val value: String) {
    init {
        require(value.isNotBlank()) { "Branch name cannot be blank" }
    }
}
