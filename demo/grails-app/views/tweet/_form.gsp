<%@ page import="demo.Tweet" %>



<div class="fieldcontain ${hasErrors(bean: tweetInstance, field: 'text', 'error')} ">
	<label for="text">
		<g:message code="tweet.text.label" default="Text" />
		
	</label>
	<g:textField name="text" value="${tweetInstance?.text}"/>
</div>

<div class="fieldcontain ${hasErrors(bean: tweetInstance, field: 'user', 'error')} required">
	<label for="user">
		<g:message code="tweet.user.label" default="User" />
		<span class="required-indicator">*</span>
	</label>
	<g:select id="user" name="user.id" from="${demo.User.list()}" optionKey="id" required="" value="${tweetInstance?.user?.id}" class="many-to-one"/>
</div>

