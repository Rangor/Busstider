package net.a2bsoft.buss.parser;

import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;


public class SaxFeedParser extends BaseFeedParser {

    public SaxFeedParser(String feedUrl){
        super(feedUrl);
    }
    
    public List<BusStop> parse() {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        try {
            SAXParser parser = factory.newSAXParser();
            XmlHandler handler = new XmlHandler();
            parser.parse(this.getInputStream(), handler);
            return handler.getNames();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } 
    }
}