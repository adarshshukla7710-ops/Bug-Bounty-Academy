package com.bugbounty.academy.ui.screens.tools

import androidx.lifecycle.ViewModel
import com.bugbounty.academy.data.repository.ToolsRepository
import com.bugbounty.academy.domain.model.SecurityTool
import com.bugbounty.academy.domain.model.ToolCategory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ToolsViewModel(
    private val toolsRepository: ToolsRepository
) : ViewModel() {

    private val _selectedTool = MutableStateFlow<SecurityTool?>(null)
    val selectedTool: StateFlow<SecurityTool?> = _selectedTool.asStateFlow()

    fun getAllTools(): List<SecurityTool> = toolsRepository.getAllTools()

    fun getToolById(toolId: String): SecurityTool? = toolsRepository.getToolById(toolId)

    fun getToolsByCategory(category: ToolCategory): List<SecurityTool> =
        toolsRepository.getToolsByCategory(category)

    fun generateCommand(template: String, paramMap: Map<String, String>): String {
        var result = template
        paramMap.forEach { (key, value) ->
            result = result.replace("{$key}", value.ifBlank { "example.com" })
        }
        return result
    }
}
