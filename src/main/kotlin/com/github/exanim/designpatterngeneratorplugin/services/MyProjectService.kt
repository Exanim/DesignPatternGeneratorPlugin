package com.github.exanim.designpatterngeneratorplugin.services

import com.intellij.openapi.components.Service

@Service(Service.Level.PROJECT)
class MyProjectService {
    fun getRandomNumber(): Int = kotlin.random.Random.nextInt()
}

