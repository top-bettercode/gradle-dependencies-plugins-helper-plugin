/*
 * Copyright (c) 2017 Peter Wu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package test.gdf.maven

import cn.bestwu.gdf.maven.GradleToMavenDependenciesCopyPasteProcessor
import org.junit.Assert
import org.junit.Test

/**
 *
 * @author Peter Wu
 * @since
 */
class Temp{

    @Test
    fun dependencyRegex() {
        Assert.assertTrue("org.jetbrains.kotlin:kotlin-stdlib:1.2.0".matches(GradleToMavenDependenciesCopyPasteProcessor.dependencyRegex))
        Assert.assertTrue("org.jetbrains.kotlin:kotlin-stdlib".matches(GradleToMavenDependenciesCopyPasteProcessor.dependencyRegex))
        Assert.assertTrue("    compile(\"org.jetbrains.kotlin:kotlin-stdlib:1.2.0\")".matches(GradleToMavenDependenciesCopyPasteProcessor.dependencyRegex))
        Assert.assertTrue("    compile \"org.jetbrains.kotlin:kotlin-stdlib:1.2.0\"".matches(GradleToMavenDependenciesCopyPasteProcessor.dependencyRegex))
        Assert.assertTrue("    compile 'org.jetbrains.kotlin:kotlin-stdlib:1.2.0'".matches(GradleToMavenDependenciesCopyPasteProcessor.dependencyRegex))
        Assert.assertTrue("    providedCompile 'org.jetbrains.kotlin:kotlin-stdlib:1.2.0'".matches(GradleToMavenDependenciesCopyPasteProcessor.dependencyRegex))
        Assert.assertTrue("    testCompile 'org.jetbrains.kotlin:kotlin-stdlib:1.2.0'".matches(GradleToMavenDependenciesCopyPasteProcessor.dependencyRegex))
        Assert.assertTrue("    provided 'org.jetbrains.kotlin:kotlin-stdlib:1.2.0'".matches(GradleToMavenDependenciesCopyPasteProcessor.dependencyRegex))
        Assert.assertTrue("    provided(\"org.jetbrains.kotlin:kotlin-stdlib:1.2.0\")".matches(GradleToMavenDependenciesCopyPasteProcessor.dependencyRegex))
        Assert.assertTrue("    providedCompile(\"org.jetbrains.kotlin:kotlin-stdlib:1.2.0\")".matches(GradleToMavenDependenciesCopyPasteProcessor.dependencyRegex))
        Assert.assertTrue("    testCompile(\"org.jetbrains.kotlin:kotlin-stdlib:1.2.0\")".matches(GradleToMavenDependenciesCopyPasteProcessor.dependencyRegex))
    }

}