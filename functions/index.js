const functions = require('firebase-functions');

const admin = require('firebase-admin');
admin.initializeApp(functions.config().firebase);

exports.pushNotification = functions.database.ref('/messages/{messageId}').onCreate((snap, context) => {

    console.log('Push notification event triggered');

    const message = snap.val();

    var ref = admin.database().ref('/users/' + message.uid);
    return ref.once("value", (snapshot) => {

		var user = snapshot.val();

	    const payload = {
	        notification: {
	            title: user.name,
	            body: message.message,
	            sound: "default"
	        },
	    };

	    const options = {
	        priority: "high",
	        timeToLive: 60 * 60 * 24
	    };


	    return admin.messaging().sendToTopic("chat_notifications", payload, options);

	    }, (errorObject) => {
	        console.log("The read failed: " + errorObject.code);
	    });
});
