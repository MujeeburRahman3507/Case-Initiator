package com.CaseTest.dto;

import java.util.List;

public class BPMRequestDTO {




    private String processDefinitionKey;
    private String businessKey;
    private List<BPMVariable> variables;

    // Getters and Setters
    public String getProcessDefinitionKey() {
        return processDefinitionKey;
    }

    public void setProcessDefinitionKey(String processDefinitionKey) {
        this.processDefinitionKey = processDefinitionKey;
    }

    public String getBusinessKey() {
        return businessKey;
    }

    public void setBusinessKey(String businessKey) {
        this.businessKey = businessKey;
    }

    public List<BPMVariable> getVariables() {
        return variables;
    }

    public void setVariables(List<BPMVariable> variables) {
        this.variables = variables;
    }

    @Override
    public String toString() {
        return "BPMRequestDTO{" +
                "processDefinitionKey='" + processDefinitionKey + '\'' +
                ", businessKey='" + businessKey + '\'' +
                ", variables=" + variables +
                '}';
    }

    // Inner class for BPMVariable
    public static class BPMVariable {

        private String name;
        private String type;
        private Object value; // Use Object to handle different types (String, Integer, etc.)

        // Getters and Setters
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public Object getValue() {
            return value;
        }

        public void setValue(Object value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return "BPMVariable{" +
                    "name='" + name + '\'' +
                    ", type='" + type + '\'' +
                    ", value=" + value +
                    '}';
        }
    }
}
