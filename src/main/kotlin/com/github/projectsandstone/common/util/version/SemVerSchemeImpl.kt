/**
 *      SandstoneCommon - Common implementation of SandstoneAPI
 *
 *         The MIT License (MIT)
 *
 *      Copyright (c) 2016 Sandstone <https://github.com/ProjectSandstone/>
 *      Copyright (c) contributors
 *
 *
 *      Permission is hereby granted, free of charge, to any person obtaining a copy
 *      of this software and associated documentation files (the "Software"), to deal
 *      in the Software without restriction, including without limitation the rights
 *      to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *      copies of the Software, and to permit persons to whom the Software is
 *      furnished to do so, subject to the following conditions:
 *
 *      The above copyright notice and this permission notice shall be included in
 *      all copies or substantial portions of the Software.
 *
 *      THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *      IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *      FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *      AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *      LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *      OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *      THE SOFTWARE.
 */
package com.github.projectsandstone.common.util.version

import com.github.projectsandstone.api.util.version.Version
import com.github.projectsandstone.api.util.version.VersionScheme

/**
 * Created by jonathan on 27/08/16.
 */
object SemVerSchemeImpl : VersionScheme {
    override fun isCompatible(version1: Version, version2: Version): Boolean {
        val ver1 = com.github.zafarkhaja.semver.Version.valueOf(version1.versionString)
        val ver2 = com.github.zafarkhaja.semver.Version.valueOf(version2.versionString)

        // SemVer: patches is always backward/upward? compatible
        // SemVer: feature addition is backward compatible, but not upward compatible
        return ver1.majorVersion == ver2.majorVersion
                && ver1.minorVersion <= ver2.minorVersion

    }

    override fun compare(o1: Version?, o2: Version?): Int {
        val ver1 = com.github.zafarkhaja.semver.Version.valueOf(o1?.versionString)
        val ver2 = com.github.zafarkhaja.semver.Version.valueOf(o2?.versionString)

        return ver1.compareTo(ver2)
    }
}