package application.service

import domain.Portfolio

interface IPortfolioService {
    suspend fun createPortfolio(capital: Double): Portfolio  // TODO : Ezt mindenképpen máshogyan kell majd csinálni, mivel egy requestben bármit beleírhat az illető -> pl.: infinite money
    suspend fun getAllPortfolio(): List<Portfolio>
}