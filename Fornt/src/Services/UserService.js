import axios from "axios";
import Axios from "./Axios";

const fetchAllUser = () => {
  return Axios.get("/api/users?page=1");
};

const getRandomProduct = () => {
  return Axios.get("/api/v1/home/random");
}

const getCategory = () => {
  const data = Axios.get("api/v1/home/all-category", {
    headers: {
      Authorization: `Bearer ${localStorage.getItem("token")}`,
    },
  });
  return data;
};



const getSubcategoryById = (category_id) => {
  const data = Axios.get('api/v1/home/find-subcategories',{
    params: {
      category_id  
    }
});
  return data;
}

const getSubCategory = () => {
  const data = Axios.get("/api/v1/home/all-subcategory");
  return data;
}

const postCreateUser = (name, job) => {
  return Axios.post("/api/users", { name, job });
};

const searchApi = (productName, token) => {
  const data = Axios.get("/api/v1/product/search",{
    params: {
      name: productName
    },
    headers: {
      Authorization: `Bearer ${token}`
    }
  });
  return data;
}

const loginApi = (username, password) => {
  return Axios.post("/api/auth/authenticate", { username, password });
};

const logoutApi = (token) => {
  return fetch("/api/v1/user/logout", {
    headers: {
      Authorization: `Bearer ${token}`,
    },
  })
    .then((response) => {
      if (!response.ok) {
        throw new Error("Logout failed");
      }
      return response;
    })
    .then((response) => response.text()) // get text instead of json
    .then((data) => {
      // handle success
    })
    .catch((error) => {
      console.error(error);
      // handle error
    });
};

export {
  fetchAllUser,
  postCreateUser,
  loginApi,
  logoutApi,
  searchApi,
  getCategory,
  getSubcategoryById,
  getSubCategory,
  getRandomProduct
};
