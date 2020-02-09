package com.altruist.templates

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.annotation.Rollback
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Stepwise

@DataJpaTest(showSql = true)
@Rollback(false)
@Stepwise
class TemplateRepoTest extends Specification {
    @Shared
    Template template = new Template()

    @Autowired
    TemplateRepo repo

    def "Should save"(){
        given:
        template.id = "ID 1"
        template.text = "text 1"

        expect:
        repo.save(template)
    }

    def "Should read"(){
        when:
        Optional<Template> maybeFound = repo.findById("ID 1")

        then:
        maybeFound.isPresent()
        Template found = maybeFound.get()
        with(found){
            id == "ID 1"
            text == "text 1"
        }
    }
}
