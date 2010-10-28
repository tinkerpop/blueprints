package com.tinkerpop.blueprints.pgm.parser;

import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

public class GraphMLEventLabeler {

	public static PipedInputStream getInputStream(
			final InputStream rawGraphMLInputStream, final String labelKey,
			final String idKey) throws IOException, XMLStreamException {

		final CountDownLatch latch = new CountDownLatch(1);

		final PipedInputStream inPipe = new PipedInputStream() {
			@Override
			public void close() throws IOException {
				super.close();
				latch.countDown();
			}
		};

		XMLInputFactory inputFactory = XMLInputFactory.newInstance();
		final XMLEventReader reader = inputFactory
				.createXMLEventReader(rawGraphMLInputStream);

		XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
		final XMLEventWriter writer = outputFactory
				.createXMLEventWriter(new PipedOutputStream(inPipe));

		new Thread(new Runnable() {
			public void run() {
				GraphMLEventLabeler labeledGraphMLInputStream;
				try {
					labeledGraphMLInputStream = new GraphMLEventLabeler(reader,
							writer, latch, labelKey, idKey);
					labeledGraphMLInputStream.label();

				} catch (XMLStreamException e) {
					throw new RuntimeException(e.getCause());
				} catch (IOException e) {
					throw new RuntimeException(e.getCause());
				}
			}
		}).start();

		return inPipe;
	}

	private XMLEventReader reader = null;
	private XMLEventWriter writer = null;

	private CountDownLatch latch = null;

	// For Edges
	private String labelKey = null;
	private String idKey = null;

	// TODO idKey for Vertices

	private GraphMLEventLabeler(XMLEventReader reader, XMLEventWriter writer,
			CountDownLatch latch, String labelKey, String idKey)
			throws XMLStreamException {
		this.labelKey = labelKey;
		this.idKey = idKey;
		this.reader = reader;
		this.writer = writer;
		this.latch = latch;
	}

	public void label() throws XMLStreamException, IOException {

		boolean inEdge = false;
		String edgeId = null;
		String edgeLabel = null;

		XMLEventFactory xmlEventFactory = XMLEventFactory.newInstance();

		ArrayList<XMLEvent> attributes = new ArrayList<XMLEvent>();
		LinkedList<XMLEvent> elements = new LinkedList<XMLEvent>();

		while (reader.hasNext()) {
			XMLEvent event = reader.nextEvent();

			if (event.getEventType() == XMLEvent.START_ELEMENT) {

				StartElement element = event.asStartElement();

				if (element.getName().getLocalPart().equals(GraphMLTokens.EDGE)) {

					Iterator<Attribute> iterator = element.getAttributes();
					while (iterator.hasNext()) {
						Attribute attribute = iterator.next();
						String name = attribute.getName().getLocalPart();
						if (name.equals(GraphMLTokens.ID))
							edgeId = attribute.getValue();
						else if (name.equals(GraphMLTokens.LABEL))
							edgeLabel = attribute.getValue();
						else
							attributes.add(attribute);
					}

					inEdge = true;

				} else if (element.getName().getLocalPart().equals(
						GraphMLTokens.DATA)) {

					if (inEdge) {
						String keyAttributeValue = element.getAttributeByName(
								new QName(GraphMLTokens.KEY)).getValue();

						if ((labelKey != null)
								&& (keyAttributeValue.equals(labelKey))) {
							// Assert it's Characters as expected
							edgeLabel = reader.nextEvent().asCharacters()
									.getData();
							// Assert it's EndElement as expected
							reader.nextEvent().asEndElement();
						} else if ((idKey != null)
								&& (keyAttributeValue.equals(idKey))) {
							// Assert it's Characters as expected
							edgeId = reader.nextEvent().asCharacters()
									.getData();
							// Assert it's EndElement as expected
							reader.nextEvent().asEndElement();
						} else {
							elements.add(element);
							// Assert it's Characters as expected
							elements.add(reader.nextEvent().asCharacters());
							// Assert it's EndElement as expected
							elements.add(reader.nextEvent().asEndElement());
						}
					} else {
						writer.add(element);
					}

				} else {
					writer.add(event);
				}

			} else if (event.getEventType() == XMLEvent.END_ELEMENT) {

				if (inEdge) {
					if (edgeLabel != null)
						attributes.add(xmlEventFactory.createAttribute(
								labelKey, edgeLabel));
					if (edgeId != null)
						attributes.add(xmlEventFactory.createAttribute(idKey,
								edgeId));

					writer.add(xmlEventFactory.createStartElement(new QName(
							GraphMLTokens.EDGE), attributes.iterator(), null));

					attributes.clear();
					while (elements.isEmpty() == false)
						writer.add(elements.remove());

					inEdge = false;
				}

				writer.add(event);

			} else if (event.getEventType() == XMLEvent.END_DOCUMENT) {

			} else {
				writer.add(event);
			}
		}

		reader.close();
		writer.flush();

		while (latch.getCount() != 0) {
			try {
				// FIXME never stops fucking "Waiting..."
				System.out.println("Waiting...");
				latch.await(500, TimeUnit.MILLISECONDS);
			} catch (InterruptedException e) {
			}
		}

		writer.close();
	}
}