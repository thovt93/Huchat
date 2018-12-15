var express = require("express");
var app = express();
var sever = require("http").Server(app);
var io = require("socket.io").listen(sever);
var fs = require("fs");

//var routes = require("./routes");
//impliment modules
var user = require("./user");
var account = require("./account");
var db = require("./database");
var mail = require("./mail");
var room = require("./room");


var arrayImage = new Array();

fs.readdir("img/", function (err, files) {
	if (err) {
		return;
	}
	files.forEach(function (f) {
		arrayImage.push("img/" + f);
	});
});

io.on("connection", function (socket) {
	var roomOfUser = [];
	socket.leave(socket.id);
	socket.emit("result", "connection", true);
	console.log("One user connected " + socket.id);
	// socket.on("user",async (userName) => {
	// 	let check = await account.checkExistUserName(userName);
	// 	console.log("have account -> ", check);
	// // });
	// socket.on("recoveryPassword", (userName, email) =>{
	// 	mail.sendMail(u)
	// });
	// mail.sendMail("tamdaulong207@gmail.com");
	socket.on("clientSendImageUser", function (data) {
		try {
			console.log("SERVER SAVED A NEW IMAGE");
			var filename = getFilenameImageUser(socket.id);
			arrayImage.push(filename);
			fs.writeFile(filename, data);
			socket.emit("result", "clientSendImageUser", true);
		} catch (ex) {
			socket.emit("result", "clientSendImageUser", false);
		}
	});

	socket.on("clientSendImageRoom", function (roomCode,data) {
		try {
			console.log("SERVER SAVED A NEW IMAGE");
			var filename = getFilenameImageRoom(roomCode);
			arrayImage.push(filename);
			fs.writeFile(filename, data);
			socket.emit("result", "clientSendImageRoom", true);
		} catch (ex) {
			socket.emit("result", "clientSendImageRoom", false);
		}
	});

	socket.on("clientRequestImageRoom", function (roomCode) {
		let dir = getFilenameImageRoom(roomCode);
		let index = arrayImage.indexOf(dir);
		let filename = index == -1 ? "" : arrayImage[index];
		fs.readFile(filename, function (err, data) {
			if (!err) {
				socket.emit("result",'severSendImageRoom', true, data, roomCode);
				//socket.emit("result", "clientSendRequestImage",true);
				console.log("SEND TO CLIENT A FILE: " + filename);
			} else {
				socket.emit("result",'severSendImageRoom', false);
				console.log('THAT BAI:'+ err + " " + filename);
			}
		});
	});

	// console.log(arrayImage);
	socket.on('clientRequestImageUser', function (userName) {
		let dir = getFilenameImageUser(userName);
		let index = arrayImage.indexOf(dir);
		let filename = index == -1 ? "" : arrayImage[index];
		fs.readFile(filename, function (err, data) {
			if (!err) {
				socket.emit("result",'severSendImageUser', true, data);
				//socket.emit("result", "clientSendRequestImage",true);
				console.log("SEND TO CLIENT A FILE: " + filename);
			} else {
				socket.emit("result",'severSendImageUser', false);
				console.log('THAT BAI: ' + filename);
			}
		});
	});

	socket.on("register", async (userName, password, email) => {
		//console.log(password);
		let existUserName = await account.checkExistUserName(userName);
		if (existUserName) {
			socket.emit("result", "register", false);
			return;
		}
		let existEmail = await account.checkExistEmail(email);
		if (existEmail) {
			socket.emit("result", "register", false);
			return;
		}
		account.registerAccount(userName, password, email, (err, rows) => {
			if (err) {
				console.log(err);
				throw err;
			}
			else {
				if (rows.affectedRows > 0) {
					//console.log("account created " + userName);
					socket.emit("result", "register", true);
				}
				else {
					socket.emit("result", "register", false);
				}
			}
		})
	});

	socket.on("login", (userName, password) => {
		// if (userName == "admin") socket.emit("result", "login", true);
		// else
		account.login(userName, password, (err, rows) => {
			if (err) {
				console.log(err);
				socket.emit("result", "login", false);
			}
			else {
				if (rows.length > 0) {
					socket.emit("result", "login", true, rows[0][0]);
					socket.userName = rows[0][0].USER_NAME;
					console.log("User " +socket.id+ " connected!");
				}
				else {
					//console.log("emited");
					socket.emit("result", "login", false);
				}
			}
		})
	});

	socket.on("logout", (userName) => {
		account.logout(userName, (err, rows) => {
			if (err) {
				console.log(err);
				socket.emit("result", "logout", false);
			}
			else {
				if (rows.affectedRows > 0) {
					console.log("account "+ userName +" logout");
					//console.log("account created " + userName);
					socket.emit("result", "logout", true);
					for(let i in roomOfUser){
						socket.leave(i);
					}
					roomOfUser = [];
				}
				else {
					socket.emit("result", "logout", false);
				}
				// socket.disconnect();
				// socket.removeAllListeners();
			}
		})
	});

	socket.on("clientRequestListRoom", (userName) => {
		room.getListRoomOfUser(userName, (err, rows) => {
			if (err) {
				socket.emit("result", "severSendListRoom", false);
			}
			else
			{	
				socket.emit("result", "severSendListRoom", true, rows[0]);
				//console.log(rows[0]);
				for(i in rows[0]){
					// console.log(socket);
					roomOfUser.push(rows[0][i].ROOM_CODE);
					socket.join(rows[0][i].ROOM_CODE);
					console.log(socket.adapter.rooms);
				}
			}
			console.log(socket.adapter.rooms);
		})
	});
	//chat
	socket.on("joinRoom", (roomCode) => {
		//find room if exist
		socket.join(roomCode);
	});

	socket.on("leaveRoom", (roomCode) => {
		//remove from list room
		socket.leave(roomCode);
	});

	socket.on("clientRequestHistoryChatRoom", (roomCode) => {
		room.getHistoryOfChatRoom(roomCode, (err, rows) =>{
			if(!err && rows.length > 0){
				// console.log(rows[0]);
				socket.emit("result", "severSendHistoryChatRoom", true, rows[0], roomCode);
			}
			else{
				socket.emit("result", "severSendHistoryChatRoom", false);
			}
		})
		
	});

	socket.on("clientSendMessage", (roomCode, userName, content) => {
		// console.log(roomCode, userName, content);
		socket.join(roomCode);
		// console.log(socket.adapter.rooms);
		
		room.userChat(roomCode, userName, content, (err, rows) => {
			io.to(roomCode).emit("severSendMessage",roomCode, userName, content);
			// socket.broadcast.to(roomCode).emit("severSendMessage",roomCode, userName, content);
			// io.sockets.to(roomCode).emit("severSendMessage",roomCode, userName, content);

		});
	});

	socket.on("disconnect", () => {
		console.log("User disconnected!");
		for(let i in roomOfUser){
			socket.leave(i);
		}
		roomOfUser = [];
		// console.log(socket.adapter.rooms);
	})
});

app.get("/", function (req, res) {
	res.sendFile(__dirname + "/index.html");
});

sever.listen(2409, () => {
	console.log("Sever is online!");
});
//app.use("/", routes);

function getFilenameImageUser(id) {
	let name = "user" + "" + id.toLowerCase();
	return "img/" + name + ".png";
}

function getFilenameImageRoom(id) {
	let name = "room" + "" + id.toLowerCase();
	return "img/" + name + ".png";
}

function getMilis() {
	var date = new Date();
	var milis = date.getTime();
	return milis;
}


module.exports = sever;


