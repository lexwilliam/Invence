package com.lexwilliam.company.route.form

import com.lexwilliam.company.model.CompanyBranch

sealed interface CompanyFormUiEvent {
    data class NameChanged(val value: String) : CompanyFormUiEvent

    data object AddBranch : CompanyFormUiEvent

    data class StepChanged(val step: Int) : CompanyFormUiEvent

    data class BranchSelected(val branch: CompanyBranch) : CompanyFormUiEvent
}