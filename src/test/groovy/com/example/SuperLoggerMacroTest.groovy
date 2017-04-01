package com.example

import groovy.transform.ToString

@ToString
class SuperLoggerMacroTest extends GroovyTestCase {

    void "test that logger works"() {
        assertScript '''
        def m =  [ a: 123 ]
        
        superlog(m.a + 5 > 100)
        '''
    }

    void "test that macro logger works"() {
        assertScript '''
        def m =  [ a: 123 ]
        
        supermacrolog(m.a + 5 > 100)
        '''
    }

    void "test nameof works"() {
        assertScript '''
        def someVar = "yo"
        
        assert nameof(someVar) == "someVar"
        '''
    }

}
