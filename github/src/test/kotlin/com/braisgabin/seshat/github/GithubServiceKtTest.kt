package com.braisgabin.seshat.github

import okhttp3.Headers
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import retrofit2.Response

class GithubServiceKtTest {

    @Test
    fun noLinks() {
        assertThat(createResponse(null).getNextUrl(), `is`(nullValue()))
    }

    @Test
    fun linksWithNextUrl() {
        val header = "<correct>; rel=\"next\", <foo>; rel=\"last\""
        assertThat(createResponse(header).getNextUrl(), `is`("correct"))
    }

    @Test
    fun linksWithNextUrl2() {
        val header = "<correct>; rel=\"next\""
        assertThat(createResponse(header).getNextUrl(), `is`("correct"))
    }

    @Test
    fun linksWithNextUrl3() {
        val header = "<foo>; rel=\"last\", <correct>; rel=\"next\""
        assertThat(createResponse(header).getNextUrl(), `is`("correct"))
    }

    @Test
    fun linksWithNextUrl4() {
        val header = "<foo>; rel=\"last\", <correct>; rel=\"first\""
        assertThat(createResponse(header).getNextUrl(), `is`(nullValue()))
    }

    private fun createResponse(header: String?): Response<*> {
        return if (header == null) {
            Response.success<Unit>(Unit)
        } else {
            Response.success<Unit>(Unit, Headers.headersOf("Link", header))
        }
    }
}
