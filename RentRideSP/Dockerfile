# Use an official Node.js runtime as a parent image
FROM node:21

# Set the working directory in the container to /app
WORKDIR /app

# Copy package.json and package-lock.json into the working directory
COPY ./package*.json ./

# Install any needed packages specified in package.json
RUN npm install

# Bundle app source inside Docker image
COPY . .

# Install dependencies for the frontend
WORKDIR /app/frontend
RUN npm install

# Switch back to the app directory
WORKDIR /app

# Make port 3000 available to the world outside this container
EXPOSE 3000

# Run npm run dev when the container launches
CMD [ "npm", "run", "dev" ]