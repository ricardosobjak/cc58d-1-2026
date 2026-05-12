const express = require('express');
const router = express.Router();

const User = require('../models/User');
const containsToken = require('../middlewares/containsToken');

// Obter a lista de usuários da API
router.get('/', containsToken, async (req, res) => {
  const { name = '' } = req.query;

  // Buscar todos usuários do banco de dados
  const users = await User.find(
    { name: { $regex: '.*' + name + '.*' } }
  );

  res.status(200).json(users);
});

// Buscar por ID
router.get("/:id", containsToken, async (req, res) => {
  const { id } = req.params;

  try {
    const user = await User.findById(id);
    return user ? res.json(user) : res.status(404).send();
  } catch (err) {
    console.log('Erro ao buscar: ', err);
    return res.status(400).json({ message: err });
  }
});

// Criar usuário
router.post("/", async (req, res) => {
  const user = new User(req.body);
  console.log(user);
  res.status(201).json(await user.save());
})


// Deletar um usuário
router.delete('/:id', containsToken, async (req, res) => {
  const { id } = req.params;
  const user = await User.findByIdAndDelete(id);
  return user ? res.json(user) : res.status(404).send();
});

//Atualizar um usuário
router.put('/:id', containsToken, async (req,res) => {
  const { id } = req.params;
  const body = req.body;

  const user = await User.findById(id);

  if(user) {
    user.name = body.name;
    user.email = body.email;
    user.password = body.password;

    return res.json(await user.save());
  } 
  else {
    return res.status(404).send();
  }
})




module.exports = router;
