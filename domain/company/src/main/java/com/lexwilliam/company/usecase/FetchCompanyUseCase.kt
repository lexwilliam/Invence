package com.lexwilliam.company.usecase

import arrow.core.Either
import com.lexwilliam.company.model.Company
import com.lexwilliam.company.repository.CompanyRepository
import com.lexwilliam.company.util.FetchCompanyFailure
import javax.inject.Inject

class FetchCompanyUseCase
    @Inject
    constructor(
        private val companyRepository: CompanyRepository
    ) {
        suspend operator fun invoke(companyUUID: String): Either<FetchCompanyFailure, Company> {
            return companyRepository.fetchCompany(companyUUID)
        }
    }