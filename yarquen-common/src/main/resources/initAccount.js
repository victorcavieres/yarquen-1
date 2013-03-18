// Execute script in mongo shell
//
// $ mongo yarquen initAccount.js

//db.roles.insert( { 
//	_id: "ADMIN", 
//	permission: ["PERM_READ_USER","PERM_WRITE_USER", "PERM_WRITE_ARTICLE", "PERM_WRITE_CATEGORY"] 
//} )
db.account.insert({
		username : "admin",
		_class : "org.yarquen.account.Account",
		email : "admin@admin.com",
		familyName : "",
		firstName : "Administrator",
		password : "d033e22ae348aeb5660fc2140aec35850c4da997",
		roleId : ["ADMIN"]
})
