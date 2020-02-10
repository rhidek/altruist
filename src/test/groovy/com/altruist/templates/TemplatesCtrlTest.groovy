package com.altruist.templates

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.jayway.jsonpath.DocumentContext
import com.jayway.jsonpath.JsonPath
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.RequestBuilder
import org.springframework.test.web.servlet.ResultActions
import spock.lang.Specification
import spock.mock.DetachedMockFactory

import javax.persistence.EntityExistsException
import javax.persistence.EntityNotFoundException

import static com.altruist.ErrorCodes.DUPLICATE_CREATION
import static com.altruist.ErrorCodes.NOT_FOUND
import static org.hamcrest.Matchers.containsString
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@WebMvcTest(controllers = [TemplatesCtrl])
class TemplatesCtrlTest extends Specification {
    @Autowired
    MockMvc mvc

    @Autowired
    ObjectMapper objectMapper

    @Autowired
    TemplatesService mockTemplatesService

    String basePath = "/templates"
    String substitutionPath = "$basePath/{templateId}/compose"

    def "Serializes DTO"() {
        given: "a template"
        String templateId = "template-id"
        String templateText = "template text"
        TemplateDTO templateDto = new TemplateDTO(templateId, templateText)

        when: "it is marshalled"
        String serialized = objectMapper.writeValueAsString(templateDto)

        then: "the serialized output contains expected properties"
        DocumentContext parsed = JsonPath.parse(serialized)
        parsed.read("\$.id") == templateId
        parsed.read("\$.text") == templateText
    }

    def "Deserializes DTO"() {
        given: "a template"
        String templateId = "template-id"
        String templateText = "template text"
        String template = """{"id":"$templateId","text":"$templateText"}"""

        when: "it is marshalled"
        TemplateDTO templateDto = objectMapper.readValue(template, TemplateDTO)

        then: "the deserialized output contains expected properties"
        with(templateDto) {
            id == templateId
            text == templateText
        }
    }

    def "Should allow saving templates"() {
        given: "a template"
        String templateId = "template-id"
        TemplateDTO template = new TemplateDTO(templateId, "message text")

        when: "the template is submitted"
        ResultActions resultActions = mvc.perform(
            post(basePath)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(template))
        )

        then: "the template is persisted"
        1 * mockTemplatesService.saveIfNotPresent(template)

        and: "the created response is returned"
        resultActions.andExpect(status().isCreated())

        and: "the location header is populated"
        resultActions.andExpect(header().exists("Location"))
            .andExpect(header().string("Location", containsString("$basePath/$templateId")))
    }

    def "Should reject overwriting templates"() {
        given: "an existing template"
        String templateId = "template-id"
        TemplateDTO template = new TemplateDTO(templateId, "message text")

        when: "the template is submitted again"
        RequestBuilder builder = post(basePath)
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(template))

        mvc.perform(builder)
        ResultActions resultActions = mvc.perform(builder)

        then: "the template is rejected"
        2 * mockTemplatesService.saveIfNotPresent(template) >> {} >> { throw new EntityExistsException("Template exists") }

        and: "the client error response is returned"
        resultActions.andExpect(status().is4xxClientError())
        resultActions.andExpect(jsonPath("\$.code").value(DUPLICATE_CREATION.toString()))
        resultActions.andExpect(jsonPath("\$.message").value("Template id existed. Please choose another template id."))
    }

    def "Should load all templates"() {
        given: "Some saved templates exist"
        List<TemplateDTO> expected = [new TemplateDTO("1", "1"), new TemplateDTO("2", "2")]
        mockTemplatesService.findAll() >> expected

        when: "all templates are requested"
        ResultActions resultActions = mvc.perform(
            get(basePath)
                .accept(MediaType.APPLICATION_JSON)
        )
        String body = resultActions.andReturn().response.getContentAsString()
        List<TemplateDTO> returned = objectMapper.readValue(body, new TypeReference<List<TemplateDTO>>() {})

        then: "the OK status is returned"
        resultActions.andExpect(status().isOk())

        and: "all templates are returned"
        returned == expected
    }

    def "Should load and substitute templates"() {
        given: "an existing template"
        String templateId = "template-id"

        and: "substitutions"
        Map<String, String> substitutions = [var1: "value 1", var2: "value 2"]
        String expected = "substituted text"

        when: "the substitution is requested"
        ResultActions resultActions = mvc.perform(
            get(substitutionPath, templateId)
                .queryParam('var1', substitutions['var1'])
                .queryParam('var2', substitutions['var2'])
                .accept(MediaType.APPLICATION_JSON)
        )

        then: "the template is loaded and substituted"
        1 * mockTemplatesService.loadAndSubstitute(templateId, substitutions) >> expected

        and: "the OK response is returned"
        resultActions.andExpect(status().isOk())

        and: "the substituted value is returned"
        resultActions.andExpect(jsonPath("\$.messageText").value(expected))
    }

    def "Should return NOT FOUND for queries using non-existent template IDs"() {
        given: "a template that does NOT exist"
        String templateId = "template-id"

        and: "substitutions"
        Map<String, String> substitutions = [var1: "value 1", var2: "value 2"]

        when: "the substitution is requested"
        ResultActions resultActions = mvc.perform(
            get(substitutionPath, templateId)
                .queryParam('var1', substitutions['var1'])
                .queryParam('var2', substitutions['var2'])
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
        )

        then: "the template is loaded and substituted"
        1 * mockTemplatesService.loadAndSubstitute(templateId, substitutions) >> { throw new EntityNotFoundException() }

        and: "the Not Found response is returned"
        resultActions.andExpect(status().isNotFound())
        resultActions.andExpect(jsonPath("\$.code").value(NOT_FOUND.toString()))
        resultActions.andExpect(jsonPath("\$.message").value("Template ID not found."))
    }

    @TestConfiguration
    static class TestConfig {
        DetachedMockFactory factory = new DetachedMockFactory()

        @Bean
        TemplatesService templatesService() {
            factory.Mock(TemplatesService)
        }

    }
}
