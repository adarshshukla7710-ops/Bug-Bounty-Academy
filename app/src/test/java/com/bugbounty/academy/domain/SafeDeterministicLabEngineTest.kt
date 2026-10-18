package com.bugbounty.academy.domain

import com.bugbounty.academy.domain.engine.SafeDeterministicLabEngine
import org.junit.Assert.*
import org.junit.Test

class SafeDeterministicLabEngineTest {

    @Test
    fun testSqliAuthBypassSuccess() {
        val result = SafeDeterministicLabEngine.execute(
            labId = "lab_sqli_auth_bypass",
            method = "POST",
            endpoint = "/api/v1/auth/login",
            headers = mapOf("Content-Type" to "application/json"),
            body = "{\"username\": \"admin' OR '1'='1\", \"password\": \"anything\"}",
            queryParams = emptyMap()
        )

        assertTrue(result.isSuccess)
        assertEquals(200, result.statusCode)
        assertTrue(result.vulnerabilityTriggered)
        assertEquals("FLAG{sqli_auth_bypass_pwned_7391}", result.flagAwarded)
        assertTrue(result.responseBody.contains("SYSTEM_ADMIN"))
    }

    @Test
    fun testSqliAuthBypassFailureOnNormalCreds() {
        val result = SafeDeterministicLabEngine.execute(
            labId = "lab_sqli_auth_bypass",
            method = "POST",
            endpoint = "/api/v1/auth/login",
            headers = mapOf("Content-Type" to "application/json"),
            body = "{\"username\": \"bob\", \"password\": \"wrongpass\"}",
            queryParams = emptyMap()
        )

        assertFalse(result.isSuccess)
        assertEquals(401, result.statusCode)
        assertNull(result.flagAwarded)
    }

    @Test
    fun testCommandInjectionPing() {
        val result = SafeDeterministicLabEngine.execute(
            labId = "lab_command_injection_ping",
            method = "POST",
            endpoint = "/api/v1/tools/ping",
            headers = mapOf("Content-Type" to "application/json"),
            body = "{\"host\": \"127.0.0.1; cat /etc/passwd\"}",
            queryParams = emptyMap()
        )

        assertTrue(result.isSuccess)
        assertEquals(200, result.statusCode)
        assertTrue(result.responseBody.contains("root:x:0:0"))
        assertEquals("FLAG{cmd_injection_pipe_escape_4821}", result.flagAwarded)
    }

    @Test
    fun testStoredXssComment() {
        val result = SafeDeterministicLabEngine.execute(
            labId = "lab_stored_xss",
            method = "POST",
            endpoint = "/api/v1/comments",
            headers = mapOf("Content-Type" to "application/json"),
            body = "{\"author\": \"attacker\", \"comment\": \"<script>alert(1)</script>\"}",
            queryParams = emptyMap()
        )

        assertTrue(result.isSuccess)
        assertEquals(201, result.statusCode)
        assertEquals("FLAG{stored_xss_comment_stealer_3840}", result.flagAwarded)
    }

    @Test
    fun testHorizontalIdorProfileAccess() {
        val result = SafeDeterministicLabEngine.execute(
            labId = "lab_idor_horizontal",
            method = "GET",
            endpoint = "/api/v1/users/1042/profile",
            headers = emptyMap(),
            body = "",
            queryParams = emptyMap()
        )

        assertTrue(result.isSuccess)
        assertEquals(200, result.statusCode)
        assertTrue(result.responseBody.contains("Sarah Connor"))
        assertEquals("FLAG{idor_horizontal_account_peek_5139}", result.flagAwarded)
    }

    @Test
    fun testVerticalPrivilegeEscalation() {
        val result = SafeDeterministicLabEngine.execute(
            labId = "lab_idor_vertical",
            method = "PUT",
            endpoint = "/api/v1/users/me",
            headers = mapOf("Content-Type" to "application/json"),
            body = "{\"name\": \"Tester\", \"role\": \"admin\"}",
            queryParams = emptyMap()
        )

        assertTrue(result.isSuccess)
        assertEquals(200, result.statusCode)
        assertEquals("FLAG{idor_vertical_role_escalation_7412}", result.flagAwarded)
    }

    @Test
    fun testMassAssignmentRegistration() {
        val result = SafeDeterministicLabEngine.execute(
            labId = "lab_mass_assignment_registration",
            method = "POST",
            endpoint = "/api/v1/auth/register",
            headers = mapOf("Content-Type" to "application/json"),
            body = "{\"username\": \"hacker\", \"password\": \"p@ss\", \"is_super_admin\": true}",
            queryParams = emptyMap()
        )

        assertTrue(result.isSuccess)
        assertEquals(201, result.statusCode)
        assertEquals("FLAG{mass_assignment_superuser_pwned_8264}", result.flagAwarded)
    }

    @Test
    fun testSsrfCloudMetadata() {
        val result = SafeDeterministicLabEngine.execute(
            labId = "lab_ssrf_cloud_metadata",
            method = "POST",
            endpoint = "/api/v1/fetch-avatar",
            headers = mapOf("Content-Type" to "application/json"),
            body = "{\"url\": \"http://169.254.169.254/latest/meta-data/iam/security-credentials/admin-role\"}",
            queryParams = emptyMap()
        )

        assertTrue(result.isSuccess)
        assertEquals(200, result.statusCode)
        assertTrue(result.responseBody.contains("SecretAccessKey"))
        assertEquals("FLAG{ssrf_cloud_metadata_iam_leaked_3498}", result.flagAwarded)
    }

    @Test
    fun testPathTraversalLfi() {
        val result = SafeDeterministicLabEngine.execute(
            labId = "lab_path_traversal_lfi",
            method = "GET",
            endpoint = "/api/v1/download?file=../../../../etc/passwd",
            headers = emptyMap(),
            body = "",
            queryParams = mapOf("file" to "../../../../etc/passwd")
        )

        assertTrue(result.isSuccess)
        assertEquals(200, result.statusCode)
        assertTrue(result.responseBody.contains("root:x:0:0"))
        assertEquals("FLAG{path_traversal_passwd_leak_8302}", result.flagAwarded)
    }

    @Test
    fun testPriceTamperingNegativeQty() {
        val result = SafeDeterministicLabEngine.execute(
            labId = "lab_logic_price_tampering",
            method = "POST",
            endpoint = "/api/v1/cart/checkout",
            headers = mapOf("Content-Type" to "application/json"),
            body = "{\"item_id\": 99, \"quantity\": -2, \"unit_price\": 500}",
            queryParams = emptyMap()
        )

        assertTrue(result.isSuccess)
        assertEquals(200, result.statusCode)
        assertEquals("FLAG{logic_negative_quantity_cash_leak_6318}", result.flagAwarded)
    }

    @Test
    fun testRaceConditionDoubleSpend() {
        val result = SafeDeterministicLabEngine.execute(
            labId = "lab_race_condition_double_spend",
            method = "POST",
            endpoint = "/api/v1/wallet/transfer",
            headers = mapOf("Content-Type" to "application/json"),
            body = "{\"recipient\": \"attacker\", \"amount\": 100, \"concurrency_burst\": 5}",
            queryParams = emptyMap()
        )

        assertTrue(result.isSuccess)
        assertEquals(200, result.statusCode)
        assertEquals("FLAG{race_condition_toctou_double_spend_7781}", result.flagAwarded)
    }

    @Test
    fun testSecureCodeReviewSqliSink() {
        val result = SafeDeterministicLabEngine.execute(
            labId = "lab_code_review_sqli_sink",
            method = "POST",
            endpoint = "/api/v1/code-review/audit-1",
            headers = mapOf("Content-Type" to "application/json"),
            body = "{\"vulnerable_line\": 14, \"vulnerability\": \"SQL_INJECTION\", \"remediation\": \"PreparedStatement\"}",
            queryParams = emptyMap()
        )

        assertTrue(result.isSuccess)
        assertEquals(200, result.statusCode)
        assertEquals("FLAG{code_review_sqli_sink_identified_3108}", result.flagAwarded)
    }

    @Test
    fun testGraphQLIntrospection() {
        val result = SafeDeterministicLabEngine.execute(
            labId = "lab_graphql_introspection_abuse",
            method = "POST",
            endpoint = "/graphql",
            headers = mapOf("Content-Type" to "application/json"),
            body = "{\"query\": \"{ __schema { types { name } } }\"}",
            queryParams = emptyMap()
        )

        assertTrue(result.isSuccess)
        assertEquals(200, result.statusCode)
        assertTrue(result.responseBody.contains("deleteUserDatabase"))
        assertEquals("FLAG{graphql_introspection_hidden_schema_8830}", result.flagAwarded)
    }
}
