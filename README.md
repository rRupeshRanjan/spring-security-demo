#Technology
1. Java 8+
2. Spring security - 2.3.1
3. Spring Boot - 2.3.1
4. Gradle 6.3

#
###### Models:
1. User - contains basic details and associated roles
2. Role - contains role name and respective privileges
3. Privilege - contains privilege details


#
###### Seed data setup (SetupDataLoader.java)
1. Does initial setup of users, roles and privileges.
2. There are 2 users in the system - User & Admin
3. User has *ROLE: USER* & Admin has *ROLE: ADMIN*
4. USER role gives *READ_PRIVILEGE*
5. ADMIN role give *READ_PRIVILEGE, WRITE_PRIVILEGE & DELETE_PRIVILEGE*

#
###### Allowed operations (AccessController.java)
- A user can see basic data (except for roles) for any user id. This is accessible for all USERs, 
while the ADMIN can see roles along with it. Respective URLs are: 
`/user/{id}` and `/admin/user/{id}` (Both are GET methods)

- ADMIN can add roles for any user through a POST call on `/user/{id}/roles` with json like
```
{
    "6" : "ADMIN",
    "7" : "USER
}
```
This assigns ADMIN role to user with id 6, and USER role to user with id 7. This action can only be done by an ADMIN, 
and a USER can't perform this operation.
 
- ADMIN can delete roles for any user through DELETE call on `/user/{id}/roles` with json like
```
{
    "6" : "ADMIN",
    "7" : "USER
}
```
This un-assigns ADMIN role from user with id 6, and USER role to user with id 7. This action can only be done by an 
ADMIN, and a USER can't perform this operation. 

- ADMIN can check if a user can take action on a certain resource, through a POST call on `/check-access/{id}` with 
json like
```
{
    "action" : "GET",
    "resource" : "/admin/user/{id}"
}
```

#
###### Access control (WebSecurityConfig.java)
1. `configureGlobal` method loads the seed-data for authentication, and any further addition ot access will reflect here.
2. `configure` method decides who can access what URLs
```
(HttpMethod.GET, "/user/*").hasAnyAuthority("USER", "ADMIN")
(HttpMethod.GET, "/admin/user/*").hasAuthority("ADMIN")
(HttpMethod.POST, "/user/*/roles").hasAuthority("ADMIN")
(HttpMethod.DELETE, "/user/*/roles").hasAuthority("ADMIN")
(HttpMethod.POST, "/check-access/*").hasAuthority("ADMIN")
```

#
###### Repositories 
1. UserRepository.java, RoleRepository.java, PrivilegeRepository.java
    1. They extend JpaRespository provided along with spring data JPA.
    2. These are used for fetching respective data from relevant tables.
2. AccessRepository.java
    1. Initializes mapping of resource access based on roles.
    2. This is used to check, given a resource and action type, does a User have access to it or not.

#
###### Service (MyUserDetailsService.class)
1. It implements UserDetailsService interface from spring security.
2. Additionally, it contains methods to support controller actions, like view, modify or query user roles.
