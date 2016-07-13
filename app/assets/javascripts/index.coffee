$ ->
    ws = new WebSocket $("body").data("ws-url")
    ws.onmessage = (event) ->
        message = JSON.parse event.data
        switch message.type
            when "message"
                $("#board tbody").append("<tr><td>#{message.uid}</td><td>#{message.guess}</td><td>#{message.hint}</td></tr>")
            else
                console.log(message)

    $("#msgform").submit (event) ->
        event.preventDefault()
        console.log($("#msgtext").val())
        ws.send(JSON.stringify({guess: $("#msgtext").val()}))
        # reset the form
        $("#msgtext").val("")
