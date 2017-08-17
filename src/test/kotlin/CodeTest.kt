import cn.bestwu.intellij.plugins.gradle.codeInsight.completion.ArtifactInfo
import org.junit.Test

/**
 *
 * @author Peter Wu
 * @since
 */


class SetTest {
    @Test
    fun set() {
        val hashset = hashSetOf<Int>(1, 3, 2)
        val linkset = linkedSetOf<Int>(1, 3, 2)
        println(hashset)
        println(linkset)
        hashset.add(3)
        linkset.add(3)
        println(hashset)
        println(linkset)
    }

    @Test
    fun ai() {
        val linkset = linkedSetOf<ArtifactInfo>()
        linkset.add(ArtifactInfo("a", "b"))
        linkset.add(ArtifactInfo("a", "b"))
        println(linkset)
    }
}