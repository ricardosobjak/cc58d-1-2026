
const containsToken = (req, res, next) => {
    const { authorization } = req.headers;

    return authorization 
        ? next()
        : res.status(401).json( { message: "sem token"});
}

module.exports = containsToken;