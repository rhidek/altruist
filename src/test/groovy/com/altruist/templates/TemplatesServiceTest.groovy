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
        1 * mockRepo.save(_) >> {Template t ->
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
}
