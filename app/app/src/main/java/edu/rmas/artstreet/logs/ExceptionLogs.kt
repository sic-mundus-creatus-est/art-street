package edu.rmas.artstreet.logs

object ExceptionLogs {
    val invalidCredentials = "The provided authentication credentials are incorrect, malformed, or expired."
    val emptyFields = "The input string is empty or null."
    val badlyFormattedEmail = "The email address is improperly formatted."
    val emailAlreadyInUse = "The email address is already associated with another account."
    val passwordTooShort = "The provided password is invalid. [Password must be at least 6 characters long.]"
}