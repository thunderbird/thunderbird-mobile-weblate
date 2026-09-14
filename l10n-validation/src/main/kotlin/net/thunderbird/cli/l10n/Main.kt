package net.thunderbird.cli.l10n

import kotlin.system.exitProcess

fun main(args: Array<String>) {
    val result = L10nValidationCli().run(args.toList())
    val output = if (result.exitCode == 0) System.out else System.err
    output.println(result.message)
    if (result.exitCode != 0) exitProcess(result.exitCode)
}
