'use strict';

const functions = require('firebase-functions');
const admin = require('firebase-admin');

admin.initializeApp(functions.config().firebase);

exports.sendChatNotification = functions.database.ref('/chats/{chatid}/chats').onWrite(event => {

    /** update chats count */
    const size = event.data.numChildren()
    console.log('size=[' + size + ']');
    event.data.ref.parent.child('count').set(size)

    /** check participants */
    return event.data.ref.parent.child('participants').once('value').then(snap => {

        const users = Object.keys(snap.val())

        console.log('users=[' + users + ']')

        var userRef = []

        users.forEach(function (user, i) {

            console.log('user=[' + user + ']')
            userRef.push(admin.database().ref().child('users').child(user).child('detail').child('token').once('value'))
        })

        console.log('userRef=[' + userRef + ']')

        /** get all token by user */
        return Promise.all(userRef).then(results => {

            console.log('results=[' + results.length + ']')

            var tokens = []

            results.forEach(function (token, i) {

                if (token.val() != null) {
                    tokens.push(token.val())
                }
            })

            console.log('tokens=[' + tokens + ']')

            /** user push */
            const payload = {
                notification: {
                    title: 'Yours',
                    body: 'Hey, you have a new message!',
                    icon: 'ic_logo_notification',
                    sound: 'default'
                },
                collapse_key: 'message',
                content_available: true,
                priority: 'high'
            }

            /** send all push */
            return admin.messaging().sendToDevice(tokens, payload).then(response => {

                console.log('response=[' + response + ']')
            })

        })

    });


});