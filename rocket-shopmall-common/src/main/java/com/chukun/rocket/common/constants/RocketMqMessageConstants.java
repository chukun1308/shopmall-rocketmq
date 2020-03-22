package com.chukun.rocket.common.constants;

/**
 * rocketmq消息常量设置
 */
public interface RocketMqMessageConstants {

    /**
     * 单name server
     */
    String SINGLE_NAME_SERVER="linux01:9876";
    /**
     * 双name server
     */
    String DOUBLE_NAME_SERVER="linux01:9876;linux02:9876";

    /**
     * 订单发送支付的事务消息生产者组
     */
    String ORDER_TO_PAY_MESSAGE_PRODUCER_GROUP = "order_to_pay_message_producer_group";

    /**
     * 订单发送支付的事务消息消费者组
     */
    String ORDER_TO_PAY_MESSAGE_CONSUMER_GROUP = "order_to_pay_message_consumer_group";

    /**
     * 订单发送支付的事务消息主题
     */
    String ORDER_TO_PAY_MESSAGE_TOPIC = "order_to_pay_message_topic";
    /**
     * 订单发送支付的事务消息主题tag
     */
    String ORDER_TO_PAY_MESSAGE_TAG = "order_to_pay_message_tag";


    /**
     * 支付发送支付平台消息生产者组
     */
    String PAY_TO_PAY_BAK_MESSAGE_PRODUCER_GROUP = "pay_to_pay_bak_message_producer_group";
    /**
     * 支付发送支付平台消息消费者组
     */
    String PAY_TO_PAY_BAK_MESSAGE_CONSUMER_GROUP = "pay_to_pay_bak_message_consumer_group";

    /**
     * 支付发送支付平台消息主题
     */
    String PAY_TO_PAY_BAK_MESSAGE_TOPIC = "pay_to_pay_bak_message_topic";
    /**
     * 支付发送支付平台消息主题tag
     */
    String PAY_TO_PAY_BAK_MESSAGE_TAG = "pay_to_pay_bak_message_tag";

    /**
     * 支付发送订单消息生产者组
     */
    String PAY_TO_ORDER_MESSAGE_PRODUCER_GROUP = "pay_to_order_message_producer_group";
    /**
     * 支付发送订单消息消费者组
     */
    String PAY_TO_ORDER_MESSAGE_CONSUMER_GROUP = "pay_to_order_message_consumer_group";

    /**
     * 支付发送订单消息主题
     */
    String PAY_TO_ORDER_MESSAGE_TOPIC = "pay_to_order_message_topic";
    /**
     * 支付发送订单消息主题tag
     */
    String PAY_TO_ORDER_MESSAGE_TAG = "pay_to_order_message_tag";

    /**
     * 订单发送物流消息生产者组
     */
    String ORDER_TO_PACKAGE_MESSAGE_PRODUCER_GROUP = "order_to_package_message_producer_group";
    /**
     * 订单发送物流消息消费者组
     */
    String ORDER_TO_PACKAGE_MESSAGE_CONSUMER_GROUP = "order_to_package_message_consumer_group";

    /**
     * 订单发送物流消息主题
     */
    String ORDER_TO_PACKAGE_MESSAGE_TOPIC = "order_to_package_message_topic";
    /**
     * 订单发送物流消息主题tag
     */
    String ORDER_TO_PACKAGE_MESSAGE_TAG = "order_to_package_message_tag";
}
