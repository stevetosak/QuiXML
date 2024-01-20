package myXml.xmlComponents;

interface XMLComponent {
    void addAttribute(String name, String val);

    String xmlString(int depth);

}

