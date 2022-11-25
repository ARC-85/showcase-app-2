package ie.wit.showcase2.api

import ie.wit.showcase2.models.PortfolioModel
import retrofit2.Call
import retrofit2.http.*


interface DonationService {
    @GET("/donations")
    fun findall(): Call<List<PortfolioModel>>

    @GET("/donations/{email}")
    fun findall(@Path("email") email: String?)
            : Call<List<PortfolioModel>>

    @GET("/donations/{email}/{id}")
    fun get(@Path("email") email: String?,
            @Path("id") id: String): Call<PortfolioModel>

    @DELETE("/donations/{email}/{id}")
    fun delete(@Path("email") email: String?,
               @Path("id") id: String): Call<DonationWrapper>

    @POST("/donations/{email}")
    fun post(@Path("email") email: String?,
             @Body donation: PortfolioModel)
            : Call<DonationWrapper>

    @PUT("/donations/{email}/{id}")
    fun put(@Path("email") email: String?,
            @Path("id") id: String,
            @Body donation: PortfolioModel
    ): Call<DonationWrapper>
}

