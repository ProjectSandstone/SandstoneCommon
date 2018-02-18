package com.github.projectsandstone.common.util.version

import com.github.projectsandstone.api.util.version.Version
import com.github.projectsandstone.api.util.version.VersionScheme

class SemVerScheme : VersionScheme {
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

class CommonVersionScheme : VersionScheme {
    override fun compare(o1: Version?, o2: Version?): Int = -1
    override fun isCompatible(version1: Version, version2: Version): Boolean = false
}

class AlphabeticVersionScheme : VersionScheme {

    override fun isCompatible(version1: Version, version2: Version): Boolean =
        version1.versionScheme == version2.versionScheme && version1.versionString == version2.versionString

    override fun compare(o1: Version, o2: Version): Int =
        if (o1.versionScheme != o2.versionScheme) -1
        else o1.versionString.compareTo(o2.versionString)

}