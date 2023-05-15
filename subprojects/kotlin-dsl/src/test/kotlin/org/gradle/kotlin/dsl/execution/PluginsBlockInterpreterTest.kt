/*
 * Copyright 2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.kotlin.dsl.execution

import org.gradle.kotlin.dsl.execution.ResidualProgram.PluginRequestSpec
import org.gradle.kotlin.dsl.support.expectedKotlinDslPluginsVersion
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.instanceOf
import org.junit.Ignore
import org.junit.Test


class PluginsBlockInterpreterTest {

    @Test
    fun `empty plugins block`() {
        assertStaticInterpretationOf("")
    }

    @Test
    fun `single plugin - id()`() {
        assertStaticInterpretationOf(
            """id("plugin-id")""",
            PluginRequestSpec("plugin-id")
        )
    }

    @Test
    fun `single plugin - id() version`() {
        assertStaticInterpretationOf(
            """id("plugin-id") version "1.0"""",
            PluginRequestSpec("plugin-id", version = "1.0")
        )
    }

    @Test
    fun `single plugin - id() version()`() {
        assertStaticInterpretationOf(
            """id("plugin-id") version("1.0")""",
            PluginRequestSpec("plugin-id", version = "1.0")
        )
    }

    @Test
    fun `single plugin - id() dot version()`() {
        assertStaticInterpretationOf(
            """id("plugin-id").version("1.0")""",
            PluginRequestSpec("plugin-id", version = "1.0")
        )
    }

    @Test
    fun `single plugin - id() apply`() {
        assertStaticInterpretationOf(
            """id("plugin-id") apply false""",
            PluginRequestSpec("plugin-id", apply = false)
        )
    }

    @Test
    fun `single plugin - id() apply()`() {
        assertStaticInterpretationOf(
            """id("plugin-id") apply(false)""",
            PluginRequestSpec("plugin-id", apply = false)
        )
    }

    @Test
    fun `single plugin - id() dot apply()`() {
        assertStaticInterpretationOf(
            """id("plugin-id").apply(false)""",
            PluginRequestSpec("plugin-id", apply = false)
        )
    }

    @Test
    fun `single plugin - id() version apply`() {
        assertStaticInterpretationOf(
            """id("plugin-id") version "1.0" apply false""",
            PluginRequestSpec("plugin-id", version = "1.0", apply = false)
        )
    }

    @Test
    fun `single plugin - id() dot version() dot apply()`() {
        assertStaticInterpretationOf(
            """id("plugin-id").version("1.0").apply(false)""",
            PluginRequestSpec("plugin-id", version = "1.0", apply = false)
        )
    }


    @Ignore("wip")
    @Test
    fun `single plugin - id() mixed version apply`() {
        assertStaticInterpretationOf(
            """id("plugin-id").version("1.0").apply(true) version "3.0" apply false""",
            PluginRequestSpec("plugin-id", version = "3.0", apply = false)
        )
    }

    @Test
    fun `single plugin - kotlin()`() {
        assertStaticInterpretationOf(
            """kotlin("js")""",
            PluginRequestSpec("org.jetbrains.kotlin.js")
        )
    }

    @Test
    fun `single plugin - kotlin() version apply false`() {
        assertStaticInterpretationOf(
            """kotlin("jvm") version "1.0" apply false""",
            PluginRequestSpec("org.jetbrains.kotlin.jvm", version = "1.0", apply = false)
        )
    }

    @Test
    fun `single plugin - kotlin-dsl`() {
        assertStaticInterpretationOf(
            """`kotlin-dsl`""",
            PluginRequestSpec("org.gradle.kotlin.kotlin-dsl", version = expectedKotlinDslPluginsVersion)
        )
    }

    @Test
    fun `multiple plugins - id()`() {
        assertStaticInterpretationOf(
            """
                id("plugin-id-1")
                id("plugin-id-2")
            """,
            PluginRequestSpec("plugin-id-1"),
            PluginRequestSpec("plugin-id-2"),
        )
    }

    @Test
    fun `multiple plugins - id() separated by semicolon`() {
        assertStaticInterpretationOf(
            """
                id("plugin-id-1") ; id("plugin-id-2")
                ;
            """,
            PluginRequestSpec("plugin-id-1"),
            PluginRequestSpec("plugin-id-2"),
        )
    }

    @Test
    fun `multiple plugins - id() version apply mixed syntax`() {
        assertStaticInterpretationOf(
            """
                id("plugin-id-1") apply false ; id("plugin-id-2")
                kotlin("jvm") version "1.0" apply false
                ;
                id("plugin-id-3") version "2.0"
            """,
            PluginRequestSpec("plugin-id-1", apply = false),
            PluginRequestSpec("plugin-id-2"),
            PluginRequestSpec("org.jetbrains.kotlin.jvm", version = "1.0", apply = false),
            PluginRequestSpec("plugin-id-3", version = "2.0"),
        )
    }

    @Ignore("wip")
    @Test
    fun `multiple plugins - multiline statement`() {
        assertStaticInterpretationOf(
            """
                id("plugin-id-1")
                    .version("1.0")
                    .apply(false)
                    .version("2.0") ; kotlin("plugin-id-2")
                    .version("1.0") apply false
                id("plugin-id-3")
            """,
            PluginRequestSpec("plugin-id-1", version = "2.0", apply = false),
            PluginRequestSpec("org.jetbrains.kotlin.plugin-id-2", version = "1.0", apply = false),
            PluginRequestSpec("plugin-id-3"),
        )
    }

    @Test
    fun `comment - line only`() {
        assertStaticInterpretationOf("// line comment\n")
    }

    @Test
    fun `comment - line`() {
        assertStaticInterpretationOf(
            """
                // line comment
                id("plugin-id-1")
                // line comment
                id("plugin-id-2")
                // line comment
            """,
            PluginRequestSpec("plugin-id-1"),
            PluginRequestSpec("plugin-id-2"),
        )
    }

    @Test
    fun `comment - block only`() {
        assertStaticInterpretationOf("/* block comment */")
    }

    @Test
    fun `comment - block`() {
        assertStaticInterpretationOf(
            """
                /* block comment */
                id("plugin-id-1")
                /* block comment */
                id("plugin-id-2")
                /* block comment */
            """,
            PluginRequestSpec("plugin-id-1"),
            PluginRequestSpec("plugin-id-2"),
        )
    }

    @Test
    fun `id - comment - block inline`() {
        assertStaticInterpretationOf(
            """
                /* block comment */ id("plugin-id-1")
                id /* block comment */ ( "plugin-id-2")
                id( /* block comment */ "plugin-id-3")
                id("plugin-id-4" /* block comment */ )
                id("plugin-id-5") /* block comment */
            """,
            PluginRequestSpec("plugin-id-1"),
            PluginRequestSpec("plugin-id-2"),
            PluginRequestSpec("plugin-id-3"),
            PluginRequestSpec("plugin-id-4"),
            PluginRequestSpec("plugin-id-5"),
        )
    }

    @Test
    fun `id version - comment - block inline`() {
        assertStaticInterpretationOf(
            """
                id("plugin-id-6") /* block comment */ .version("1.0")
                id("plugin-id-7"). /* block comment */ version("1.0")
                id("plugin-id-8") /* block comment */ version "1.0"
                id("plugin-id-9") version /* block comment */ "1.0"
            """,
            PluginRequestSpec("plugin-id-6", version = "1.0"),
            PluginRequestSpec("plugin-id-7", version = "1.0"),
            PluginRequestSpec("plugin-id-8", version = "1.0"),
            PluginRequestSpec("plugin-id-9", version = "1.0"),
        )
    }

    @Test
    fun `id - comment - block multiline`() {
        assertStaticInterpretationOf(
            """

                /* multiline
                block
                comment */ id("plugin-id-1")

                id /* multiline
                block
                comment */ ("plugin-id-2")

                id( /* multiline
                block
                comment */ "plugin-id-3")

                id("plugin-id-4" /* multiline
                block
                comment */ )

                id("plugin-id-5") /* multiline
                block
                comment */

                id("plugin-id-11")
            """,
            PluginRequestSpec("plugin-id-1"),
            PluginRequestSpec("plugin-id-2"),
            PluginRequestSpec("plugin-id-3"),
            PluginRequestSpec("plugin-id-4"),
            PluginRequestSpec("plugin-id-5"),
            PluginRequestSpec("plugin-id-11"),
        )
    }

    @Test
    fun `id version - comment - block multiline`() {
        assertStaticInterpretationOf(
            """
                id("plugin-id-6") /* multiline
                block
                comment */ .version("1.0")

                id("plugin-id-7"). /* multiline
                block
                comment */ version("1.0")

                id("plugin-id-8") /* multiline
                block
                comment */ version "1.0"

                id("plugin-id-9") version /* multiline
                block
                comment */ "1.0"

                id("plugin-id-10") version "1.0" /* multiline
                block
                comment */
                id("plugin-id-11")
            """,
            PluginRequestSpec("plugin-id-6", version = "1.0"),
            PluginRequestSpec("plugin-id-7", version = "1.0"),
            PluginRequestSpec("plugin-id-8", version = "1.0"),
            PluginRequestSpec("plugin-id-9", version = "1.0"),
            PluginRequestSpec("plugin-id-10", version = "1.0"),
            PluginRequestSpec("plugin-id-11"),
        )
    }

    @Test
    fun `comment - kdoc only`() {
        assertStaticInterpretationOf("/** kdoc comment */")
    }

    @Test
    fun `comment - kdoc`() {
        assertStaticInterpretationOf(
            """
                /** kdoc comment */
                id("plugin-id-1")
                /** kdoc comment */
                id("plugin-id-2")
                /** kdoc comment */
            """,
            PluginRequestSpec("plugin-id-1"),
            PluginRequestSpec("plugin-id-2"),
        )
    }

    @Test
    fun `comment - kdoc inline`() {
        assertStaticInterpretationOf(
            """
                /** kdoc comment */ id("plugin-id-1")
                id /** kdoc comment */ ( "plugin-id-2")
                id( /** kdoc comment */ "plugin-id-3")
                id("plugin-id-4" /** kdoc comment */ )
                id("plugin-id-5") /** kdoc comment */
            """,
            PluginRequestSpec("plugin-id-1"),
            PluginRequestSpec("plugin-id-2"),
            PluginRequestSpec("plugin-id-3"),
            PluginRequestSpec("plugin-id-4"),
            PluginRequestSpec("plugin-id-5"),
        )
    }

    @Test
    fun `id version - comment - kdoc inline`() {
        assertStaticInterpretationOf(
            """
                id("plugin-id-6") /** kdoc comment */ .version("1.0")
                id("plugin-id-7"). /** kdoc comment */ version("1.0")
                id("plugin-id-8") /** kdoc comment */ version "1.0"
                id("plugin-id-9") version /** kdoc comment */ "1.0"
            """,
            PluginRequestSpec("plugin-id-6", version = "1.0"),
            PluginRequestSpec("plugin-id-7", version = "1.0"),
            PluginRequestSpec("plugin-id-8", version = "1.0"),
            PluginRequestSpec("plugin-id-9", version = "1.0"),
        )
    }

    @Test
    fun `id - comment - kdoc multiline`() {
        assertStaticInterpretationOf(
            """

                /** multiline
                kdoc
                comment */ id("plugin-id-1")

                id /** multiline
                kdoc
                comment */ ("plugin-id-2")

                id( /** multiline
                kdoc
                comment */ "plugin-id-3")

                id("plugin-id-4" /** multiline
                kdoc
                comment */ )

                id("plugin-id-5") /** multiline
                kdoc
                comment */

                id("plugin-id-6") /** multiline
                kdoc
                comment */ .version("1.0")

                id("plugin-id-7"). /** multiline
                kdoc
                comment */ version("1.0")

                id("plugin-id-8") /** multiline
                kdoc
                comment */ version "1.0"

                id("plugin-id-9") version /** multiline
                kdoc
                comment */ "1.0"

                id("plugin-id-10") version "1.0" /** multiline
                kdoc
                comment */
                id("plugin-id-11")
            """,
            PluginRequestSpec("plugin-id-1"),
            PluginRequestSpec("plugin-id-2"),
            PluginRequestSpec("plugin-id-3"),
            PluginRequestSpec("plugin-id-4"),
            PluginRequestSpec("plugin-id-5"),
            PluginRequestSpec("plugin-id-6", version = "1.0"),
            PluginRequestSpec("plugin-id-7", version = "1.0"),
            PluginRequestSpec("plugin-id-8", version = "1.0"),
            PluginRequestSpec("plugin-id-9", version = "1.0"),
            PluginRequestSpec("plugin-id-10", version = "1.0"),
            PluginRequestSpec("plugin-id-11"),
        )
    }

    @Test
    fun `unsupported syntax - plugin spec accessor`() {
        assertDynamicInterpretationOf(
            """java""",
            "Expecting id or kotlin, got 'java'"
        )
    }

    @Test
    fun `unsupported syntax - version catalog alias`() {
        assertDynamicInterpretationOf(
            """alias(libs.plugins.jmh)""",
            "Expecting id or kotlin, got 'alias'"
        )
    }

    @Test
    fun `syntax error - starts with unknown identifier`() {
        assertDynamicInterpretationOf(
            """garbage""",
            "Expecting id or kotlin, got 'garbage'"
        )
    }

    @Test
    fun `syntax error - starts with unexpected token`() {
        assertDynamicInterpretationOf(
            """.""",
            "Expecting plugin spec, got '.'"
        )
    }

    @Test
    fun `syntax error - id() without parens`() {
        assertDynamicInterpretationOf(
            """id "plugin-id"""",
            "Expecting (, got '\"'"
        )
    }

    @Test
    fun `syntax error - id() with not a string`() {
        assertDynamicInterpretationOf(
            """id(false)""",
            "Expecting <plugin id string>, got 'false'"
        )
    }

    @Test
    fun `syntax error - id() with empty string`() {
        assertDynamicInterpretationOf(
            """id("")""",
            "Expecting <plugin id string>, got '\"'"
        )
    }

    @Test
    fun `syntax error - id() with unclosed string`() {
        assertDynamicInterpretationOf(
            """id("plugin-id-1) ; id("plugin-id-2")"""",
            "Expecting ), got 'plugin'"
        )
    }

    @Test
    fun `syntax error - id() with unclosed parens`() {
        assertDynamicInterpretationOf(
            """id("plugin-id-1" ; id("plugin-id-2")"""",
            "Expecting ), got ';'"
        )
    }

    @Test
    fun `syntax error - id() with misplaced semicolon`() {
        assertDynamicInterpretationOf(
            """id("plugin-id-1";)""",
            "Expecting ), got ';'"
        )
    }

    @Test
    fun `syntax error - id() apply with not a boolean`() {
        assertDynamicInterpretationOf(
            """id("plugin-id") apply "1.0"""",
            "Expecting (, got '\"'"
        )
    }

    @Test
    fun `syntax error - id() version with not a string`() {
        assertDynamicInterpretationOf(
            """id("plugin-id") version false""",
            "Expecting (, got 'false'"
        )
    }

    @Test
    fun `syntax error - id() id() on the same line`() {
        assertDynamicInterpretationOf(
            """id("plugin-id-1") id("plugin-id-2")""",
            "Expecting <statement separator>, got 'id'"
        )
    }

    @Test
    fun `syntax error - id() unknown on the same line`() {
        assertDynamicInterpretationOf(
            """id("plugin-id-1") unknown "thing"""",
            "Expecting version or apply, got 'unknown'"
        )
    }

    @Test
    fun `syntax error - kotlin() without parens`() {
        assertDynamicInterpretationOf(
            """kotlin "plugin-id"""",
            "Expecting (, got '\"'"
        )
    }

    @Test
    fun `syntax error - kotlin() with not a string`() {
        assertDynamicInterpretationOf(
            """kotlin(false)""",
            "Expecting <kotlin plugin module string>, got 'false'"
        )
    }

    @Test
    fun `syntax error - kotlin() with empty string`() {
        assertDynamicInterpretationOf(
            """kotlin("")""",
            "Expecting <kotlin plugin module string>, got '\"'"
        )
    }

    @Test
    fun `syntax error - kotlin() with unclosed string`() {
        assertDynamicInterpretationOf(
            """kotlin("plugin-id-1) ; kotlin("plugin-id-2")"""",
            "Expecting ), got 'plugin'"
        )
    }

    @Test
    fun `syntax error - kotlin() with unclosed parens`() {
        assertDynamicInterpretationOf(
            """kotlin("plugin-id-1" ; kotlin("plugin-id-2")"""",
            "Expecting ), got ';'"
        )
    }

    @Test
    fun `syntax error - kotlin() with misplaced semicolon`() {
        assertDynamicInterpretationOf(
            """kotlin("plugin-id-1";)""",
            "Expecting ), got ';'"
        )
    }

    @Test
    fun `syntax error - kotlin() apply with not a boolean`() {
        assertDynamicInterpretationOf(
            """kotlin("plugin-id") apply "1.0"""",
            "Expecting (, got '\"'"
        )
    }

    @Test
    fun `syntax error - kotlin() version with not a string`() {
        assertDynamicInterpretationOf(
            """kotlin("plugin-id") version false""",
            "Expecting (, got 'false'"
        )
    }

    @Test
    fun `syntax error - kotlin() kotlin() on the same line`() {
        assertDynamicInterpretationOf(
            """kotlin("plugin-id-1") kotlin("plugin-id-2")""",
            "Expecting <statement separator>, got 'kotlin'"
        )
    }

    @Test
    fun `syntax error - kotlin() unknown on the same line`() {
        assertDynamicInterpretationOf(
            """kotlin("plugin-id-1") unknown "thing"""",
            "Expecting version or apply, got 'unknown'"
        )
    }

    @Test
    fun `syntax error - dot after dot`() {
        assertDynamicInterpretationOf(
            """id("plugin-id-1"). .version("1.0")""",
            "Expecting version or apply, got '.'"
        )
    }

    @Test
    fun `syntax error - dot after dot on next line`() {
        assertDynamicInterpretationOf(
            """
                id("plugin-id-1").
                    .version("1.0")
            """,
            "Expecting version or apply, got '.'"
        )
    }

    @Test
    fun `syntax error - id() dot version`() {
        assertDynamicInterpretationOf(
            """id("plugin-id").version "1.0"""",
            """Expecting (, got '"'"""
        )
    }

    @Test
    fun `syntax error - id() dot apply`() {
        assertDynamicInterpretationOf(
            """id("plugin-id").apply false""",
            """Expecting (, got 'false'"""
        )
    }

    private
    fun assertStaticInterpretationOf(pluginsBlock: String, vararg specs: PluginRequestSpec) {
        assertThat(
            interpret(Program.Plugins(fragment("plugins", pluginsBlock))),
            equalTo(
                PluginsBlockInterpretation.Static(specs.asList())
            )
        )
    }

    private
    fun assertDynamicInterpretationOf(pluginsBlock: String, @Suppress("unused_parameter") reason: String) {
        assertThat(
            interpret(Program.Plugins(fragment("plugins", pluginsBlock))),
            instanceOf(PluginsBlockInterpretation.Dynamic::class.java)
// TODO: validate failure reason
//            equalTo(
//                PluginsBlockInterpretation.Dynamic(reason)
//            )
        )
    }
}
