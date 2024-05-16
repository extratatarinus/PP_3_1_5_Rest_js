const URLCurrentUser = "/api/user/getCurrentUser";
const URLListUsers = "/api/admin/getUsers";
const URLGetUserById = "/api/admin/getUser/";
const URLUpdate = "api/admin/update";
const URLDelete = "/api/admin/delete/";
const URLAddNew = "/api/admin/add";
const URLAllRoles = "/api/admin/findAllRoles";
const URLLogout = "/logout";
let top_panel_info = document.getElementById('top_panel_info');
let table_user_info = document.getElementById('table_user_info');
let formEdit = document.forms["formEdit"];
let formDelete = document.forms["formDelete"];
let formNew = document.forms["formNew"];
let currId;
let currUser;

function getCurrentUser() {
    fetch(URLCurrentUser)
        .then((res) => res.json())
        .then((currentUser) => {
            currId = currentUser.id;
            currUser = currentUser.email;

            let currentRoles = getCurrentRoles(currentUser.roles);
            top_panel_info.innerHTML = `
                <span>Вы вошли как: <b>${currentUser.email}</b></span>
                <span> | Обладает ролью:</span>
                <span>${currentRoles}</span>
            `;

            table_user_info.innerHTML = `
                <tr>
                    <td>${currentUser.id}</td>
                    <td>${currentUser.email}</td>
                    <td>${currentUser.firstname}</td>
                    <td>${currentUser.lastname}</td>
                    <td>${currentUser.age}</td>
                    <td>${currentRoles}</td>
                </tr>`;
        });
}

getCurrentUser();

function getCurrentRoles(roles) {
    let currentRoles = "";
    for (let role of roles) {
        currentRoles += (role.name.toString().substring(5) + " ");
    }
    return currentRoles;
}

window.addEventListener("load", () => loadUserRoles());

async function loadUserRoles(where) {
    let disabled = "";
    let edit_check_roles;
    if (where == "upd") {
        edit_check_roles = await document.getElementById("edit_check_roles");
    } else if (where == "del") {
        edit_check_roles = await document.getElementById("delete_check_roles");
        disabled = "disabled";
    } else {
        edit_check_roles = await document.getElementById("new_check_roles");
        where = "new";
    }
    if (edit_check_roles != null) {
        await edit_check_roles.replaceChildren();
    }

    await fetch(URLAllRoles)
        .then(res => res.json())
        .then(roles => {
            roles.forEach(role => {
                let inputId = role.name + '_' + where;

                edit_check_roles.innerHTML += `
                    <label class="font-weight-bold" 
                    for="${role.name}">${role.name.substring(5)}</label>
                    <input class="messageCheckbox" 
                    type="checkbox" 
                    value="${role.id}" 
                    id="${inputId}"
                    name="${role.name.substring(5)}"
                    ${disabled}>
                `;
            });
        });
}

async function getUserById(id) {
    let response = await fetch(URLGetUserById + id);
    return await response.json();
}

function getUsers() {
    fetch(URLListUsers)
        .then(response => response.json())
        .then(users => {
            const users_table_fill = document.getElementById('users_table_fill');
            users_table_fill.innerHTML = "";
            for (let user of users) {
                let userRoles = getCurrentRoles(user.roles);

                let fill_users = `
                    <tr>
                        <td>${user.id}</td>
                        <td>${user.email}</td>
                        <td>${user.name}</td>
                        <td>${user.lastName}</td>
                        <td>${user.age}</td>
                        <td>${userRoles}</td>
                        <td>
                            <button type="button"
                            class="btn btn-info btn-ml"
                            data-bs-toggle="modal"
                            data-bs-target="#editModal"
                            onclick="modalEdit(${user.id})">
                                Редактировать
                            </button>
                        </td>
                        <td>
                            <button type="button" 
                            class="btn btn-danger btn-ml" 
                            data-bs-toggle="modal" 
                            data-bs-target="#deleteModal" 
                            onclick="modalDelete(${user.id})">
                                Удалить
                            </button>
                        </td>
                    </tr>`;
                users_table_fill.innerHTML += fill_users;
            }
        });
}

getUsers();

async function fill_modal(form, modal, id, where) {
    modal.show();
    let user = await getUserById(id);
    form.id.value = user.id;
    form.name.value = user.name;
    form.lastName.value = user.lastName;
    form.age.value = user.age;
    form.email.value = user.email;
    form.password.value = user.password;

    for (let role of user.roles) {
        let inputId = role.name + '_' + where;
        document.getElementById(inputId).checked = true;
    }
}

async function modalEdit(id) {
    const modalEdit = new bootstrap.Modal(document.querySelector('#modalEdit'));
    await loadUserRoles("upd");
    await fill_modal(formEdit, modalEdit, id, "upd");
}

function editUser() {
    let check400 = false;
    formEdit.addEventListener("submit", ev => {
        ev.preventDefault();
        let usernameField = document.getElementById('username_upd');

        let rolesForEdit = [];
        let inputElements = document.getElementsByClassName('messageCheckbox');
        for (let i = 0; inputElements[i]; ++i) {
            if (inputElements[i].checked) {
                rolesForEdit.push({
                    id: inputElements[i].value,
                    name: "ROLE_" + inputElements[i].name
                })
            }
        }
        fetch(URLUpdate, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                id: formEdit.id.value,
                email: formEdit.email.value,
                name: formEdit.name.value,
                lastName: formEdit.lastName.value,
                age: formEdit.age.value,
                password: formEdit.password.value,
                roles: rolesForEdit
            })
        })
            .then(response => {
                check400 = checkStatus(response, usernameField);
            })
            .then(() => {
                if (check400) {
                    getUsers();
                } else {
                    if (formEdit.id.value == currId
                        && formEdit.email.value != currUser) {
                        window.location.assign(URLLogout);
                    }
                    let hasAdmin = false;
                    rolesForEdit.forEach(role => {
                        if (role.name == "ROLE_ADMIN") {
                            hasAdmin = true;
                        }
                    })
                    if (formEdit.id.value == currId && rolesForEdit.length > 0 && !hasAdmin) {
                        window.location.assign(URLLogout);
                    }
                    document.getElementById('editClose').click();
                    getCurrentUser();
                    getUsers();

                }
            });
    });
}

editUser();

async function modalDelete(id) {
    const modalDelete = new bootstrap.Modal(document.querySelector('#modalDelete'));
    await loadUserRoles("del");
    await fill_modal(formDelete, modalDelete, id, "del");
}

function deleteUser() {
    formDelete.addEventListener("submit", ev => {
        ev.preventDefault();
        fetch(URLDelete + formDelete.id.value, {
            method: 'DELETE',
            headers: {
                'Content-Type': 'application/json'
            }
        }).then(() => {
            if (formDelete.id.value == currId) {
                window.location.assign(URLLogout);
            }
            document.getElementById('deleteClose').click();
            getUsers();
        });
    });
}

deleteUser();

function addNew() {
    formNew.addEventListener("submit", async ev => {
        ev.preventDefault();
        let emailField = document.getElementById('email_new');

        let existingUsers = await fetch(URLListUsers)
            .then(response => response.json());

        let emailExists = existingUsers.some(user => user.email === formNew.email.value);

        if (emailExists) {
            emailField.classList.add('is-invalid');
            let errorDiv = document.createElement('div');
            errorDiv.id = 'errorDiv';
            errorDiv.innerText = 'Этот адрес электронной почты уже используется.';
            emailField.parentElement.append(errorDiv);
            setTimeout(() => {
                erase(emailField);
            }, 3000);
            return;
        }

        let rolesForNew = [];
        let inputElements = [
            document.getElementById('ROLE_ADMIN_new'),
            document.getElementById('ROLE_USER_new')
        ];

        for (let inputElement of inputElements) {
            if (inputElement.checked) {
                rolesForNew.push({
                    id: inputElement.value,
                    name: "ROLE_" + inputElement.name
                });
            }
        }

        let response = await fetch(URLAddNew, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                email: formNew.email.value,
                name: formNew.name.value,
                lastName: formNew.lastName.value,
                age: formNew.age.value,
                password: formNew.password.value,
                roles: rolesForNew
            })
        });

        let check400 = checkStatus(response, emailField);

        if (check400) {
            getUsers();
        } else {
            formNew.reset();
            getUsers();
            document.getElementById('users-list-tab').click();
        }
    });
}

addNew();

function erase(emailField) {
    emailField.classList.remove('is-invalid');
    let err;
    if ((err = document.getElementById('errorDiv')) != null) {
        err.remove();
    }
}

function checkStatus(response, emailField) {
    if (response.status === 400) {
        emailField.classList.add('is-invalid');
        let errorDiv = document.createElement('div');
        errorDiv.id = 'errorDiv';
        errorDiv.innerText = 'Имя пользователя должно быть уникальным';
        emailField.parentElement.append(errorDiv);
        setTimeout(() => {
            erase(emailField);
        }, 3000);
        return true;
    } else {
        return false;
    }
}
