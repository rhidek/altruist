package com.altruist.templates

import com.altruist.ErrorCodes
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

import static com.altruist.ErrorCodes.DUPLICATE_CREATION
import static org.hamcrest.Matchers.containsString
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(controllers = [TemplatesCtrl])
class TemplatesCtrlTest extends Specification {
    @Autowired
    MockMvc mvc

    @Autowired
    ObjectMapper objectMapper

    @Autowired
    TemplatesService mockTemplatesService

    String basePath = "/templates"

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
        1 * mockTemplatesService.save(template)

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
        2 * mockTemplatesService.save(template) >> {} >> { throw new EntityExistsException("Template exists") }

        and: "the client error response is returned"
        resultActions.andExpect(status().is4xxClientError())

        and: "the location header is populated"
        resultActions.andExpect(jsonPath("\$.code",).value(DUPLICATE_CREATION.toString()))
        resultActions.andExpect(jsonPath("\$.message",).value("Template id existed. Please choose another template id."))
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
