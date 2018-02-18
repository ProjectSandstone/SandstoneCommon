package com.github.projectsandstone.common.test.platform

import com.github.projectsandstone.api.Implementation
import com.github.projectsandstone.api.util.version.Schemes
import com.github.projectsandstone.api.util.version.Version

class TestImplementation : Implementation {
    override val name: String = "TestPlatform"
    override val fullName: String = "TestPlatform"
    override val version: Version = Version("1.0.0", Schemes.semVerScheme)
    override val designedMcVersion: Version = Version("1.12.2", Schemes.commonVersionScheme)
    override val designedApiVersion: Version = Version("1.0.0", Schemes.semVerScheme)
}