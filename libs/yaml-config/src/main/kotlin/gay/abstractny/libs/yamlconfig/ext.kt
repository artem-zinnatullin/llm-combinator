package gay.abstractny.libs.yamlconfig

import com.charleskorn.kaml.Yaml
import kotlinx.serialization.decodeFromString
import org.intellij.lang.annotations.Language

fun parseYamlConfig(@Language("yaml") content: String): YamlConfig {
    try {
        return Yaml.default.decodeFromString(content)
    } catch (e: Throwable) {
        // This message clearly points to a YAML structure issue for user.
        System.err.println(e)
        throw e
    }
}
