package com.altruist.templates

import spock.lang.Specification

import javax.persistence.EntityExistsException

class TemplatesServiceTest extends Specification {
    TemplateRepo mockRepo = Mock()
    TemplatesService service = new TemplatesService(mockRepo)

    def "Should marshall DTO and call save"() {
        given: "a DTO"
        String id = "id 1"
        String text = "text 1"
        TemplateDTO dto = new TemplateDTO(id, text)

        and: "the DTO has not yet been saved"
        mockRepo.findById(id) >> Optional.ofNullable()

        when: "save is called"
        service.saveIfNotPresent(dto)

        then: "the DTO is marshalled to an entity and passed to the underlying repo"
        1 * mockRepo.save(_) >> { Template t ->
            assert t.id == id
            assert t.text == text
        }
    }

    def "Should reject previously saved templates"() {
        given: "a DTO"
        String id = "id 1"
        String text = "text 1"
        TemplateDTO dto = new TemplateDTO(id, text)

        and: "the DTO has been previously saved"
        mockRepo.findById(id) >> Optional.of(new Template(id: id, text: text))

        when: "save is called"
        service.saveIfNotPresent(dto)

        then: "the save is rejected"
        thrown(EntityExistsException)
    }

    def "Should load all templates"() {
        given: "some templates exist"
        mockRepo.findAll() >> [new Template(id: "1", text: "1"), new Template(id: "2", text: "2")]

        when: "findAll is called"
        List<TemplateDTO> all = service.findAll()

        then: "all templates are loaded"
        all.size() == 2
        all[0].id == "1"
        all[0].text == "1"
        all[1].id == "2"
        all[1].text == "2"
    }

    def "Should perform template substitutions"() {
        given: "a template with variables"
        String id = "1"
        String text = 'text $var1 text $var2text'
        Template template = new Template(id: id, text: text)
        mockRepo.findById(id) >> Optional.of(template)

        and: "variable substitutions"
        Map<String, String> substitutions = [var1: "1234", var2: "4321"]

        when: "a substitution request is made"
        String substituted = service.loadAndSubstitute(id, substitutions)

        then: "the template is returned with the variables substituted"
        substituted == 'text 1234 text 4321text'
    }
}
