package streams

import org.junit.After
import org.junit.Before
import org.junit.Test
import org.neo4j.test.rule.DbmsRule
import org.neo4j.test.rule.ImpermanentDbmsRule
import streams.kafka.KafkaSinkConfiguration
import streams.service.TopicType
import streams.service.Topics
import kotlin.test.assertEquals

class StreamsTopicServiceTest {

    private lateinit var streamsTopicService: StreamsTopicService
    private lateinit var kafkaConfig: KafkaSinkConfiguration

    @Before
    fun setUp() {
        kafkaConfig = KafkaSinkConfiguration(streamsSinkConfiguration = StreamsSinkConfiguration(topics = Topics(cypherTopics = mapOf("shouldWriteCypherQuery" to "MERGE (n:Label {id: event.id})\n" +
                "    ON CREATE SET n += event.properties"))))
        streamsTopicService = StreamsTopicService()
        streamsTopicService.set(TopicType.CYPHER, kafkaConfig.streamsSinkConfiguration.topics.cypherTopics)
    }

    private fun assertProperty(entry: Map.Entry<String, String>) {
        assertEquals(entry.value, streamsTopicService.getCypherTemplate(entry.key))
    }

    @Test
    fun shouldStoreTopicAndCypherTemplate() {
        kafkaConfig.streamsSinkConfiguration.topics.cypherTopics.forEach { assertProperty(it) }
    }

    @Test
    fun shouldStoreTopicsAndCypherTemplate() {
        // given
        val map = mapOf("topic1" to "MERGE (n:Label1 {id: event.id})",
                "topic2" to "MERGE (n:Label2 {id: event.id})")

        // when
        streamsTopicService.set(TopicType.CYPHER, map)

        // then
        val allTopics = map.plus(kafkaConfig.streamsSinkConfiguration.topics.cypherTopics)
        allTopics.forEach { assertProperty(it) }

        assertEquals(allTopics, streamsTopicService.getAll().getValue(TopicType.CYPHER))

        assertEquals(allTopics.keys, streamsTopicService.getTopics())

    }

    @Test
    fun `should remove topics`() {
        // given
        val topicToRemove = "topic2"
        val map = mapOf("topic1" to "MERGE (n:Label1 {id: event.id})",
                topicToRemove to "MERGE (n:Label2 {id: event.id})")

        streamsTopicService.set(TopicType.CYPHER, map)
        val allTopics = map.plus(kafkaConfig.streamsSinkConfiguration.topics.cypherTopics)
        allTopics.forEach { assertProperty(it) }

        // when
        streamsTopicService.remove(TopicType.CYPHER, topicToRemove)

        // then
        val remainingTopics = allTopics.filterKeys { it != topicToRemove }

        assertEquals(remainingTopics, streamsTopicService.getAll().getValue(TopicType.CYPHER))

        assertEquals(remainingTopics.keys, streamsTopicService.getTopics())

    }
}