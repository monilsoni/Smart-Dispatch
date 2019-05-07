const functions = require('firebase-functions');
const admin = require('firebase-admin')
admin.initializeApp(functions.config().firebase)

function euclideanDistance(x, y, clusters, k){
	var cl=0, ldist=10000, dist=0
	for (let i=0; i<k; i++){
		dist = Math.sqrt(Math.pow(x - clusters[i].x, 2) + Math.pow(y - clusters[i].y, 2))
		//console.log(dist)
		if (dist < ldist){
			ldist = dist
			cl = i
		}

	}
	//console.log(ldist)
	//console.log(mse)
	return [cl, ldist]
}

function KMeans(arr, cnt, vehicles){
	var prevMse=0;
	for (var k=2; k<=50; k++){
	var clusters = [], density = []
	for (let i=1; i<=k; i++){
	let x = 22 + 2*Math.random()
	let y = 72 + 2*Math.random()
	clusters.push({"x":x.toFixed(8), "y": y.toFixed(8)})
	density.push([])
	}

	var epochs = 10, mse=0
	for (let i = 1; i <= epochs; i++){
		mse=0
		for (let j = 0; j<cnt; j++){
				let d = euclideanDistance(arr[j].x, arr[j].y, clusters, k)
				
				density[d[0]].push(arr[j])
				mse+=d[1]
		}
		//console.log(density)
		for (let j=0; j<k; j++){
			let sux=0, suy=0, iter=0
			for(let q in density[j]){
				sux+=density[j][q].x
				suy+=density[j][q].y
				iter++
			}
			//console.log(sux)
			if (iter!==0){
			sux = sux/iter
			suy = suy/iter
			clusters[j].x = sux.toFixed(8) 
			clusters[j].y = suy.toFixed(8)
			}
			density[j] = []
		}
		//console.log(mse)
	}
	if(k!==2){
		if(prevMse - mse <= 1){

			break
		}
	}
		prevMse = mse
}
var vehi = [], c = 0, count=0
for (let p=0; p<k;p++){
	c = Math.floor(density[p].length / cnt * vehicles)
	count+=c
	vehi.push(c)
}
for (let p=1; p<=vehicles-count; p++){
	let ind = Math.floor(k*Math.random())
	vehi[ind]+=1
}

console.log("done")
				for (p=0; p<k; p++){
					console.log(clusters[p].x)
					admin.firestore().collection('clusters').doc(p+'').set({x:clusters[p].x, y:clusters[p].y, noOfVehicles:vehi[p]})
				}
return vehi
}

exports.retrieve = functions.https.onRequest((request, response)=>{
	var coor = [], cnt=0
	admin.firestore().collection('clusters').get().then((snap)=>{
	snap.forEach(doc=>{
		doc.ref.delete()

	})
	return null
})
	.catch((error)=>
	console.log(error))
	admin.firestore().collection('coor').get().then((snapshot)=>{
		snapshot.forEach(doc=>{
			coor.push({"x":doc._fieldsProto.x.doubleValue, "y":doc._fieldsProto.y.doubleValue})
			cnt = cnt+1;
		})
		var Allocation = KMeans(coor, cnt, parseInt(request.query.vehicles))
		return response.send(Allocation)

	}).catch((error)=>
	console.log(error))
	// /response.send(error))
	//console.log(cnt)
	//console.log(coor)
	//var Allocation = KMeans(coor, cnt, 50)
	//response.json(Allocation)

})




exports.addVehicle = functions.https.onRequest((request, response)=>{
	/*var i = 2;

	for(i; i<=49; i++){
		admin.firestore().collection('Vehicles').document(i).add()
	}*/
	admin.firestore().collection('clusters').get().then(function(snap){
	snap.forEach(doc=>{
		doc.ref.delete()
	})
	return null
    })
	.catch((error)=>
		console.log(error))

	admin.firestore().collection('clusters').doc().collection('Vehicles').get().then(function(snap){
	snap.forEach(doc=>{
		doc.ref.delete()
	})
	return null
    })
	.catch((error)=>
		console.log(error))

	admin.firestore().collection('Vehicles').doc('1').get().then(function(snap){
		console.log(snap.data())
		for (var i=2; i<=49; i++){
			admin.firestore().collection('Vehicles').doc(i+'').set(snap.data()).then(()=>
				console.log("hi"))
			.catch(()=>
				console.log("NO")
			)
		}
		return response.json({"msg":"msg"})

	})
	.catch(function(error){
		console.log(error)
		return response.json({"msg2":"msg2"})
	})
})

exports.addCoor = functions.https.onRequest((request, response)=>{
	

	for(var i=0; i<100; i++){
		let x = 22.000000 + 2*Math.random();
	    let y = 72.000000 + 2*Math.random();

		admin.firestore().collection('coor').add({"x":x,"y":y}).then(()=>
						console.log("dope")).catch(()=>console.log("not dope"))
	}
})


exports.sendNotifHospital = functions.https.onRequest((request, response)=>{
	//var registrationToken = "dvrQU2V61bo:APA91bE4Bkeb86tVAklq6mFw6dZP9fAJ2-fGxz-s4kmLnbac0vYNGpMVmR55w-JbbXzVQV1wQ_2aiq7KP_6UDt0e4_K-XLvIIduNMC_Dfn1ikm0siaHOvRglxs3Hh50pVg8hsOAewUxN"
	//var uid = "foINo2rUmnh5HQ6cidBpi9296N43"
var registrationToken;
//admin.database()
//.ref("123/request")
//.push({"req":"Request : Emergency"})

admin.firestore().collection("Hospitals").doc(request.query.id).get().then(function(snap){
registrationToken = snap._fieldsProto.token.stringValue;
console.log(registrationToken)
var message = {
  notification: {
    title: 'Emergency',
    body: request.query.name + ' in Danger'
  },
  data: {
  	user: "hospital",
  	type: "connected"
  },
  token: registrationToken
};
// Send a message to the device corresponding to the provided
// registration token.
admin.messaging().send(message)
return response.send("Done")
}).catch((error)=>{
  return response.send(error)
})
});

exports.sendNotifVehicle = functions.https.onRequest((request, response)=>{
	//var registrationToken = "dvrQU2V61bo:APA91bE4Bkeb86tVAklq6mFw6dZP9fAJ2-fGxz-s4kmLnbac0vYNGpMVmR55w-JbbXzVQV1wQ_2aiq7KP_6UDt0e4_K-XLvIIduNMC_Dfn1ikm0siaHOvRglxs3Hh50pVg8hsOAewUxN"
	//var uid = "foINo2rUmnh5HQ6cidBpi9296N43"
var registrationToken;
//admin.database()
//.ref("123/request")
//.push({"req":"Request : Emergency"})

admin.firestore().collection("Vehicles").doc(request.query.id).get().then(function(snap){
registrationToken = snap._fieldsProto.token.stringValue;
console.log(registrationToken)
var message = {
  notification: {
    title: 'Emergency',
    body: request.query.name + ' in Danger'
  },
  data: {
  	user: "vehicle",
  	type: "connected"
  },
  token: registrationToken
};
// Send a message to the device corresponding to the provided
// registration token.
admin.messaging().send(message)
return response.send("Done")
}).catch((error)=>{
  return response.send(error)
})
});

exports.sendNotifRequester = functions.https.onRequest((request, response)=>{
  //var registrationToken = "dvrQU2V61bo:APA91bE4Bkeb86tVAklq6mFw6dZP9fAJ2-fGxz-s4kmLnbac0vYNGpMVmR55w-JbbXzVQV1wQ_2aiq7KP_6UDt0e4_K-XLvIIduNMC_Dfn1ikm0siaHOvRglxs3Hh50pVg8hsOAewUxN"
  //var uid = "foINo2rUmnh5HQ6cidBpi9296N43"
var registrationToken;
//admin.database()
//.ref("123/request")
//.push({"req":"Request : Emergency"})

admin.firestore().collection("Users").doc(request.query.id).get().then(function(snap){
registrationToken = snap._fieldsProto.token.stringValue;
console.log(registrationToken)
var message = {
  notification: {
    title: request.query.name + ' will be reaching you',
    body: request.query.no
  },
  data: {
  	user: "requester",
  	type: "connected"
  },
  token: registrationToken
};
// Send a message to the device corresponding to the provided
// registration token.
admin.messaging().send(message)
return response.send("Done")
}).catch((error)=>{
  return response.send(error)
})
});

exports.sendNotifRequesterReached = functions.https.onRequest((request, response)=>{
  //var registrationToken = "dvrQU2V61bo:APA91bE4Bkeb86tVAklq6mFw6dZP9fAJ2-fGxz-s4kmLnbac0vYNGpMVmR55w-JbbXzVQV1wQ_2aiq7KP_6UDt0e4_K-XLvIIduNMC_Dfn1ikm0siaHOvRglxs3Hh50pVg8hsOAewUxN"
  //var uid = "foINo2rUmnh5HQ6cidBpi9296N43"
var registrationToken;
//admin.database()
//.ref("123/request")
//.push({"req":"Request : Emergency"})

admin.firestore().collection("Users").doc(request.query.id).get().then(function(snap){
registrationToken = snap._fieldsProto.token.stringValue;
console.log(registrationToken)
var message = {
  notification: {
    title: request.query.name + ' has reached',
    body: request.query.no
  },
  data: {
  	user: "requester",
  	type: "reached"
  },
  token: registrationToken
};
// Send a message to the device corresponding to the provided
// registration token.
admin.messaging().send(message)
return response.send("Done")
}).catch((error)=>{
  return response.send(error)
})
});

exports.sendNotifHospitalReached = functions.https.onRequest((request, response)=>{
  //var registrationToken = "dvrQU2V61bo:APA91bE4Bkeb86tVAklq6mFw6dZP9fAJ2-fGxz-s4kmLnbac0vYNGpMVmR55w-JbbXzVQV1wQ_2aiq7KP_6UDt0e4_K-XLvIIduNMC_Dfn1ikm0siaHOvRglxs3Hh50pVg8hsOAewUxN"
  //var uid = "foINo2rUmnh5HQ6cidBpi9296N43"
var registrationToken;
//admin.database()
//.ref("123/request")
//.push({"req":"Request : Emergency"})

admin.firestore().collection("Hospitals").doc(request.query.id).get().then(function(snap){
registrationToken = snap._fieldsProto.token.stringValue;
console.log(registrationToken)
var message = {
  notification: {
    title: request.query.name + ' has reached the location',
    body: request.query.no
  },
  data: {
  	user: "hospital",
  	type: "reached"
  },
  token: registrationToken
};
// Send a message to the device corresponding to the provided
// registration token.
admin.messaging().send(message)
return response.send("Done")
}).catch((error)=>{
  return response.send(error)
})
});

exports.sendNotifRequesterRequestEnd = functions.https.onRequest((request, response)=>{
  //var registrationToken = "dvrQU2V61bo:APA91bE4Bkeb86tVAklq6mFw6dZP9fAJ2-fGxz-s4kmLnbac0vYNGpMVmR55w-JbbXzVQV1wQ_2aiq7KP_6UDt0e4_K-XLvIIduNMC_Dfn1ikm0siaHOvRglxs3Hh50pVg8hsOAewUxN"
  //var uid = "foINo2rUmnh5HQ6cidBpi9296N43"
var registrationToken;
//admin.database()
//.ref("123/request")
//.push({"req":"Request : Emergency"})

admin.firestore().collection("Users").doc(request.query.id).get().then(function(snap){
registrationToken = snap._fieldsProto.token.stringValue;
console.log(registrationToken)
var message = {
  notification: {
    title: 'Request',
    body: 'Request has ended'
  },
  data: {
  	user: "requester",
  	type: "ended"
  },
  token: registrationToken
};
// Send a message to the device corresponding to the provided
// registration token.
admin.messaging().send(message)
return response.send("Done")
}).catch((error)=>{
  return response.send(error)
})
});

exports.sendNotifVehicleRequestEnd = functions.https.onRequest((request, response)=>{
  //var registrationToken = "dvrQU2V61bo:APA91bE4Bkeb86tVAklq6mFw6dZP9fAJ2-fGxz-s4kmLnbac0vYNGpMVmR55w-JbbXzVQV1wQ_2aiq7KP_6UDt0e4_K-XLvIIduNMC_Dfn1ikm0siaHOvRglxs3Hh50pVg8hsOAewUxN"
  //var uid = "foINo2rUmnh5HQ6cidBpi9296N43"
var registrationToken;
//admin.database()
//.ref("123/request")
//.push({"req":"Request : Emergency"})

admin.firestore().collection("Vehicles").doc(request.query.id).get().then(function(snap){
registrationToken = snap._fieldsProto.token.stringValue;
console.log(registrationToken)
var message = {
  notification: {
    title: 'Request',
    body: 'Request has ended'
  },
  data: {
  	user: "vehicle",
  	type: "ended"
  },
  token: registrationToken
};
// Send a message to the device corresponding to the provided
// registration token.
admin.messaging().send(message)
return response.send("Done")
}).catch((error)=>{
  return response.send(error)
})
});
