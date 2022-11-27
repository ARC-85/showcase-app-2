package ie.wit.showcase2.models

interface PortfolioStore {
    fun findAll(): List<PortfolioModel>
    fun create(portfolio: PortfolioModel)
    fun update(portfolio: PortfolioModel)
    fun delete(portfolio: PortfolioModel)
    fun createProject(project: NewProject, portfolio: PortfolioModel)
    fun updateProject(project: NewProject, portfolio: PortfolioModel)
    fun deleteProject(project: NewProject, portfolio: PortfolioModel)
    fun findProjects(): List<NewProject>
    fun findProject(id: Long): NewProject?
    fun findPortfolio(portfolio: PortfolioModel): PortfolioModel?
    fun findSpecificPortfolios(portfolioType: String): List<PortfolioModel>
    fun findSpecificTypeProjects(portfolioType: String): MutableList<NewProject>
    fun findPortfolioById(id: Long): PortfolioModel?
    fun findProjectById(projectId: Long, portfolioId: Long): NewProject?
}

